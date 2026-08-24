# Инструкции Codex для Sound

Эти инструкции действуют для всего репозитория. Перед структурными изменениями прочитай [ARCHITECTURE.md](ARCHITECTURE.md). Перед созданием или изменением тестов прочитай [TESTING.md](TESTING.md); тестовые правила здесь намеренно не повторяются.

## Сохраняй фактический scope

- Выполняй только запрошенное изменение и необходимые для него локальные исправления.
- Не реорганизуй соседние feature, package или Gradle configuration без явной необходимости.
- Рабочее дерево может содержать изменения пользователя. Не перезаписывай, не откатывай и не форматируй несвязанные файлы.
- Перед редактированием проверь `git status` и изучи пересекающийся diff.
- Новые generated files, Room schemas и Hilt/provider files проверь в финальном `git status`, чтобы они не остались незамеченными.

## Архитектурные границы

- `Domain` не импортирует Android, Room, Media3, Compose, `Data`, `Presentation` или `service`.
- `Data` реализует domain contracts и владеет Room, MediaStore, Cursor, файловой системой и Android `Context`.
- `Presentation` зависит от domain models и interfaces, а не от concrete data implementations.
- `service` управляет Media3 runtime и зависит от domain interfaces; DAO в service не передавай.
- Hilt wiring не помещай в Domain.
- Не создавай новый Gradle-модуль без отдельного запроса: текущая архитектура одномодульная.

Use case добавляй только для самостоятельной policy, transformation, orchestration или переиспользуемого правила. Для простого вызова одного repository method используй repository напрямую из ViewModel или другого application boundary.

## Размещение нового кода

- Новые package names пиши lowercase.
- Не продолжай существующие опечатки `defualtQueue` и `utills` в новых package.
- Физический путь файла должен соответствовать package.
- Domain model не должен содержать Android `Uri`; используй устойчивое строковое представление на domain boundary.
- Room entity/domain mapping держи рядом с persistence model или в явно названном mapper того же data feature.
- UI-only models не помещай в Domain.

Не выполняй массовое переименование legacy packages в рамках обычной feature-задачи. Мигрируй затронутый участок только если это безопасно и не раздувает diff.

## Compose

- Разделяй stateful route/screen и stateless content, когда screen получает ViewModel или framework dependencies.
- Route собирает state lifecycle-aware и передаёт content обычные значения и callbacks.
- Content composable не знает о Room, repository, Hilt или Media3 controller.
- Composable принимает `Modifier`, если он представляет экран или переиспользуемый visual component.
- Пользовательский текст и accessibility descriptions выноси в string resources.
- Локально храни только краткоживущее визуальное состояние; screen/business state принадлежит ViewModel.
- Добавляй Preview для нового самостоятельного content/component, если его можно представить без Android service или Hilt graph.
- Не передавай ViewModel глубже UI-дерева, если достаточно state и callbacks.

Существующие hardcoded строки и прямые ViewModel parameters не являются образцом для нового UI.

## State management

- Публично expose immutable `StateFlow`/`Flow`, а не mutable implementation.
- Для связного feature state предпочитай один immutable `UiState`.
- Sealed events используй, когда они образуют устойчивый закрытый UI contract; не создавай event type ради одного очевидного callback.
- Не копируй repository Flow в новый `MutableStateFlow` без derived state, lifecycle или sharing-причины.
- Не хранить в Compose состояние, которое должно пережить recomposition/navigation и влияет на domain operation.
- Ошибки, loading и empty state различай явно; пустой успешный список не должен означать бесконечную загрузку.

## Coroutines и Flow

- У каждой coroutine должен быть понятный lifecycle owner.
- ViewModel work запускай в `viewModelScope`; service work — в scope, отменяемом в `onDestroy`.
- Не создавай ad-hoc process scope в UI или feature без необходимости.
- Пробрасывай `CancellationException` и отменяй устаревшую работу, если результат больше не нужен.
- Не блокируй main thread Room, MediaStore, hashing или файловым IO.
- Dispatcher делай внедряемым, когда выбор потока является частью поведения или конфигурации компонента.
- Для callback API используй Flow с корректным unregister/`awaitClose`.
- Multi-step изменения одного инварианта выполняй в Room transaction.
- Не допускай параллельной записи media transitions, если порядок влияет на current song или queue consumption.

## Data и Room

- DAO и entities не выходят из Data layer.
- Repository возвращает domain models и domain-oriented Flow.
- Сохраняй identity queue item при mapping в MediaItem и обратно.
- При изменении Room schema повышай version, добавляй migration и обновляй экспортированную schema.
- Не добавляй persistence path, который только записывается и нигде не читается.
- При изменении денормализованной `Song` проверь все entity mappings: queue, player state, default queue и edit overlay.
- Metadata edit остаётся overlay поверх MediaStore, пока пользователь явно не запросит запись тегов в файл.
- Сохранённые обложки должны использовать внутреннее storage и существующую deduplication boundary.

## Playback и Media3

- ExoPlayer и MediaSession принадлежат `PlaybackService`.
- Compose и обычные ViewModel не получают ExoPlayer напрямую.
- `PlayerController` — adapter между MediaController callbacks и `PlayerUiState`.
- Domain не должен знать о `MediaItem`, `Player`, `MediaController` или `MediaMetadata`.
- Playback state сначала сохраняется через repository, затем service синхронизирует player из aggregate state.
- Фактический media transition сохраняется последовательно и атомарно потребляет explicit queue item.
- Не пересоздавай весь playlist, если достаточно заменить current/upcoming items без потери позиции.
- Runtime-created объект, зависящий от конкретного ExoPlayer, создавай рядом с владельцем player; устойчивые collaborators предоставляй через DI.

## Navigation

- Используй сериализуемые typed routes из `Routes`.
- Передавай через route стабильный ID, а не крупный mutable object.
- `toRoute<T>()` должен декодировать точно тот тип, которым зарегистрирован destination.
- Явно выбирай ViewModel scope: Activity, graph или destination; не полагайся на случайный `LocalViewModelStoreOwner`.
- Navigation callbacks остаются на route/NavHost boundary; content composable получает обычные callbacks.
- Не сравнивай typed route через нестабильный `toString`, если доступен type-safe route check.

## Dependency injection

- Constructor injection — основной способ передачи зависимостей.
- Interface-to-implementation bindings объявляй в явном Hilt module подходящего scope.
- `@Singleton` используй только для process-wide state или ресурса, который действительно должен быть общим.
- Runtime object не превращай в singleton только для удобства injection.
- Для заменяемой зависимости предпочти interface/provider boundary; concrete production class не открывай без runtime-причины.
- В новом коде не смешивай `javax.inject` и `jakarta.inject`; следуй одному выбранному namespace в затронутом участке.
- При добавлении binding убедись, что provider/module файл tracked и Hilt graph компилируется.

## Gradle

- Новые зависимости добавляй через version catalog, если нет причины использовать прямую координату.
- Не обновляй версии библиотек попутно с feature/fix.
- Не добавляй machine-specific пути, proxy или credentials в tracked Gradle files.
- Не меняй signing/minification/release configuration без отдельной задачи.
- Избегай дублирующих координат и нескольких версий одной библиотеки.

## Legacy boundaries

Не используй как reference:

- `FakeSongProvider` из production source set;
- domain wrapper `PlayerState`, который только оборачивает `Song`;
- write-only default queue flow;
- mutable `SortButtonValue`;
- ручное проксирование одного Flow в другой state;
- hardcoded Russian UI strings;
- package names с заглавными буквами и существующие опечатки;
- неверный `SongEditRoute` decoder в текущем `AppNavHost`;
- edit-save, который запускает вложенную ViewModel coroutine перед немедленным pop.

Если задача касается такого участка, не расширяй legacy-паттерн. Исправь затронутый slice в направлении, описанном в `ARCHITECTURE.md`, не начиная несвязанную миграцию всего проекта.

## Завершение задачи

- Проверь, что изменение находится в правильном слое и не создаёт обратную dependency.
- Проверь lifecycle, cancellation, source of truth и transaction boundary затронутого потока.
- При navigation change проверь route type и ViewModel scope.
- Выполни проверки, предписанные [TESTING.md](TESTING.md) для соответствующего типа изменения.
- Просмотри финальный diff и `git status`; не включай несвязанные изменения пользователя.
- В отчёте отделяй выполненные проверки от тех, которые не удалось запустить, и указывай причину.
