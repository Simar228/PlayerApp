# Testing Sound

Этот файл описывает, как добавлять и изменять JUnit-тесты в проекте Sound. Он фиксирует фактический стек и предпочтительные тестовые границы проекта, а не является общим руководством по Android-тестированию.

## Текущий стек

- JUnit 4 — test runner и rules.
- Truth — основные assertions.
- `kotlinx-coroutines-test` — coroutine и ViewModel tests.
- Room Testing — in-memory database в instrumentation tests.
- Media3 Test Utils — `FakePlayer`, `DummyMainThread` и MediaSession tests.
- Compose UI Test — для поведения composable-интерфейса; сейчас подключён, но существующих UI-тестов нет.
- Ручные fake-реализации предпочтительнее больших Mockito setup, когда зависимость имеет небольшой domain-интерфейс.

Production-код находится в `app/src/main`, локальные JVM-тесты — в `app/src/test`, Android/instrumentation-тесты — в `app/src/androidTest`.

## Выбор типа теста

Используй самый дешёвый уровень, на котором доступно проверяемое поведение.

| Изменение | Основной тип теста |
|---|---|
| Чистая сортировка, mapping или use case | JVM unit test |
| ViewModel, cancellation, StateFlow, UI events | JVM unit test с `runTest` и fake-зависимостями |
| Repository без Android/Room implementation | JVM unit test |
| DAO, Room transaction, entity mapping, ordering | Instrumentation test с in-memory Room |
| MediaController, MediaSession, настоящий callback Media3 | Instrumentation test с Media3 fakes |
| Навигационный или пользовательский Compose-сценарий | Compose instrumentation test |
| Только внешний вид composable без поведения | Preview; JUnit-тест обычно не нужен |

Не поднимай Room, MediaSession или Compose runtime, если правило можно проверить как чистую Kotlin-функцию.

## Что должен проверять тест

Проверяй наблюдаемое поведение через публичную границу класса:

- возвращаемое значение;
- опубликованный `StateFlow`/`Flow`;
- вызов repository/controller через fake;
- сохранённое состояние Room;
- cancellation и порядок операций, если они являются частью контракта;
- реакцию UI на пользовательское действие.

Не привязывай тест к private-методам, числу внутренних `collect`, конкретному DAO-запросу или случайной последовательности implementation calls. Исключение допустимо только для lifecycle API, которое невозможно вызвать публично; существующий reflection-вызов `ViewModel.onCleared()` следует считать временным компромиссом, а не шаблоном для новых API.

## Имена и структура

Для Kotlin-тестов используй имя, описывающее условие и результат:

```kotlin
@Test
fun `sendSong when controller is ready starts playback immediately`() = runTest {
    // Arrange
    playerController.emitState(
        PlayerUiState(connectionState = PlayerConnectionState.Ready)
    )

    // Act
    sut.sendSong(song = FakeSong.SONG_0)
    advanceUntilIdle()

    // Assert
    assertThat(repository.startPlaybackCalls.single().song)
        .isEqualTo(FakeSong.SONG_0)
}
```

Придерживайся Arrange–Act–Assert, но не добавляй комментарии, если три части и без них очевидны. Тест должен иметь одну поведенческую причину падения. Несколько assertions допустимы, когда они описывают один результат.

Тестируемый объект называй `sut`. Общую настройку помещай в `@Before` только тогда, когда она действительно одинакова для большинства тестов.

## Test doubles

Предпочтительный порядок:

1. Простая реальная реализация для чистого кода.
2. Ручной fake для repository/controller/provider.
3. Framework fake, например Media3 `FakePlayer`.
4. Mockito — только если ручной fake стал сложнее тестируемого сценария или нужно проверить редкий callback/API boundary.

Fake должен хранить только состояние, необходимое тестам, и реализовывать тот же публичный контракт, что production dependency. Размещай fake в соответствующем test source set, а не в `main`.

Не делай production-класс `open` только ради наследования в тесте. Если объект нужно заменять, предпочти небольшой interface или provider boundary. Хороший fake позволяет:

- установить входное состояние;
- записать вызовы в типизированный список;
- вручную инициировать callback;
- при необходимости имитировать suspension, failure или cancellation.

Не используй строки вроде `"play"` как call model, если аргументы важны: создай `data class PlayCall(...)` или sealed test event.

## Coroutines

Coroutine-тесты запускай через `runTest`:

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()

@Test
fun `new request cancels unfinished playback`() = runTest {
    repository.suspendSong = FakeSong.SONG_0

    sut.sendSong(song = FakeSong.SONG_0)
    runCurrent()
    sut.sendSong(song = FakeSong.SONG_1)
    advanceUntilIdle()

    assertThat(repository.cancelledSongs)
        .containsExactly(FakeSong.SONG_0)
}
```

Правила:

- не используй `Thread.sleep`;
- `runCurrent()` выполняет уже запланированную работу без продвижения виртуального времени;
- `advanceTimeBy()` используй для debounce, delay и polling;
- `advanceUntilIdle()` используй только если фоновые jobs способны стать idle;
- бесконечные collectors/polling jobs явно останавливай в `finally` или через lifecycle API;
- `CancellationException` не преобразовывай в обычную ошибку;
- production dispatcher по возможности внедряй, а не фиксируй внутри класса;
- проверяй не только успешный результат, но и отмену устаревшей работы, если класс хранит `Job`.

`MainDispatcherRule` проекта находится в `app/src/test/java/com/example/sound/utill/MainDispatcherRule.kt`. Не создавай локальную копию rule в каждом package.

## Flow и StateFlow

Для `StateFlow` сначала проверяй синхронное `value`, если этого достаточно. Для конечного Room/data flow допустим `first()`. Для последовательности emissions используй Turbine либо явный collector в `runTest`; не добавляй delays, чтобы «дать Flow время».

Проверяй значимые переходы состояния, например:

```text
Connecting -> pending request -> Ready -> playback started
```

Не проверяй повторно саму реализацию `MutableStateFlow`. Тест должен доказывать, что входное событие публикует правильное domain/UI state.

Если source Flow бесконечный, collector должен быть привязан к `backgroundScope` либо явно отменён до завершения теста.

## ViewModel tests

ViewModel-тест должен создавать реальный ViewModel и заменять только его внешние зависимости.

Минимальный набор сценариев для stateful ViewModel:

- начальное состояние;
- успешная обработка основного события;
- error/invalid input, если такой контракт существует;
- повторный вызов и идемпотентность;
- cancellation предыдущей операции;
- очистка ресурсов при завершении lifecycle;
- forwarding UI event к controller/repository, если ViewModel является boundary.

Не создавай отдельный use case mock для каждого однострочного repository-вызова. В этом проекте use case оправдан, когда содержит policy, преобразование или несколько операций — например, `MoveQueueItemUseCase` и `SaveQueueOrderUseCase`.

Reference implementations:

- `app/src/test/java/com/example/sound/Presentation/playerUi/viewModel/PlayerViewModelTest.kt`
- `app/src/test/java/com/example/sound/Presentation/songQueue/SongQueueViewModelTest.kt`
- `app/src/test/java/com/example/sound/Domain/useCase/queue/MoveQueueItemUseCaseTest.kt`

## Room и repository instrumentation tests

Room-тесты выполняй в `androidTest` с `InMemoryDatabaseRule`:

```kotlin
@RunWith(AndroidJUnit4::class)
class PlayerQueueRepositoryImplTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule(AppDatabase::class.java)

    @Test
    fun deleteQueueItem_reindexesRemainingItems() = runTest {
        val repository = PlayerQueueRepositoryImpl(
            queueDao = dbRule.database.queueDao(),
            database = dbRule.database,
        )

        repository.insertSongAtTheEnd(FakeSong.SONG_0)
        repository.insertSongAtTheEnd(FakeSong.SONG_1)
        val deletedId = repository.observeQueue().first()[0].id

        repository.deleteQueueItemById(deletedId)

        val result = repository.observeQueue().first()
        assertThat(result.map { it.position }).containsExactly(0)
    }
}
```

Для Room проверяй:

- entity ↔ domain mapping;
- порядок и unique constraints;
- reindex после insert/delete/reorder;
- атомарный результат multi-table transaction;
- первую и последующую Flow emission;
- поведение при неизвестных или повторяющихся IDs;
- migration при повышении версии схемы.

Не используй `allowMainThreadQueries()` как основание писать production-код для main thread: это только настройка тестовой in-memory базы.

Reference implementation: `app/src/androidTest/java/com/example/sound/Data/repository/PlayerQueueRepositoryImplTest.kt`.

## Media3 instrumentation tests

Media3 controller/session проверяй на Android runtime. Используй `FakePlayer` и соблюдай thread ownership:

- `FakePlayer` создавай и освобождай на его `DummyMainThread`;
- `MediaSession` и release `MediaController` выполняй на Android main thread;
- дождись подключения `controllerFuture` до assertions;
- освобождай controller, session, player и dummy thread в `@After`, включая частично завершившийся setup.

Проверяй преобразование реального Media3 callback в `PlayerUiState`, reconnect/release guards, media transitions, seek и playback state. Не подменяй MediaController Mockito-моком, если смысл теста — интеграция с MediaSession.

Reference implementation: `app/src/androidTest/java/com/example/sound/Presentation/playerUi/viewModel/PlayerControllerTest.kt`.

## Compose tests

Добавляй Compose UI test, когда поведение нельзя надёжно доказать через ViewModel:

- клик вызывает правильный callback;
- loading/error/content действительно переключаются;
- queue reorder/delete доступен пользователю;
- navigation destination открывается с правильным typed route;
- semantics/content description доступны accessibility-тесту.

Передавай в content composable обычные значения и callbacks. Не поднимай Hilt/NavHost/реальный repository, если тестируется один компонент. Полный NavHost используй только для navigation scenario.

Ищи nodes по semantics, text resource или content description, а не по координатам и внутренней структуре layout.

## Обязательные regression tests

При исправлении дефекта сначала добавь тест, который падает на старом поведении и проходит после исправления. Для текущих архитектурных рисков особенно важны тесты на:

- пустую, но успешно загруженную MediaStore library;
- точный тип `toRoute` для `SongEditRoute`;
- завершение edit-save до уничтожения destination ViewModel;
- восстановление сохранённого default queue order;
- последовательное потребление queue item при media transition;
- отмену предыдущего playback request;
- повторное подключение и release PlayerController.

## Команды

Из корня проекта на Windows:

```powershell
# Все JVM unit tests
.\gradlew.bat testDebugUnitTest --console=plain

# Один test class
.\gradlew.bat testDebugUnitTest --tests "com.example.sound.Presentation.songQueue.SongQueueViewModelTest" --console=plain

# Instrumentation tests; требуется подключённое устройство или emulator
.\gradlew.bat connectedDebugAndroidTest --console=plain

# Android lint
.\gradlew.bat lintDebug --console=plain
```

Не утверждай, что instrumentation tests прошли, если ADB-устройства не было. В отчёте разделяй результаты JVM tests, compilation/lint и Android tests.

## Definition of done

Тестовая работа завершена, когда:

- выбран минимальный подходящий test level;
- проверяется публичное поведение, а не implementation detail;
- coroutine jobs и Android/Media3 resources освобождаются;
- happy path и значимые failure/cancellation cases покрыты;
- тест стабилен без sleeps и зависимости от порядка запуска;
- новые fakes находятся только в test source set;
- `testDebugUnitTest` проходит;
- `connectedDebugAndroidTest` запущен при изменении Room/Media3/Compose и наличии устройства;
- `lintDebug` не добавляет новых предупреждений, связанных с изменением;
- `git status` проверен на новые schema, test fixtures и забытые untracked-файлы.
