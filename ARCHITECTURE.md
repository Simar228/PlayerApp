# Архитектура Sound

Этот документ описывает фактическую архитектуру текущего репозитория. Он отделяет устойчивые решения от незавершённых изменений и legacy-кода; это не описание желаемой много-модульной архитектуры.

Правила написания и запуска тестов вынесены в [TESTING.md](TESTING.md) и здесь не повторяются.

## Краткая модель

Sound — одномодульное Android-приложение на Compose с package-based layered architecture и MVVM-подобным presentation layer.

```text
Android entry points
   |                 |
   v                 v
Presentation -----> Domain <----- Data
   |
   v
service ----------> Domain

Hilt связывает domain-интерфейсы с data-реализациями.
MediaController связывает UI с MediaSessionService.
```

Физический Gradle-модуль только один — `:app`. Архитектурные границы задаются package-структурой и соглашениями, а не Gradle dependency graph.

## Слои и пакеты

### Корневой package

`com.example.sound` содержит Android entry points:

- `SoundApplication` запускает Hilt и инициализацию системных жанров;
- `MainActivity` управляет разрешением на чтение аудио и выбирает loading, permission, error или основной UI.

Файл `MainActivity.kt` физически находится в каталоге `Presentation/activity`, но объявлен в корневом package. Это существующая аномалия структуры, а не соглашение для новых файлов.

### Presentation

`com.example.sound.Presentation` содержит:

- Compose screens и переиспользуемые components;
- `@HiltViewModel`;
- UI state и UI events;
- type-safe Navigation Compose routes;
- adapter между ViewModel и Media3 `MediaController`.

Presentation зависит от domain models/repository interfaces. Прямые зависимости от `service` ограничены созданием `SessionToken` и общим MediaItem mapping на границе player controller.

### Domain

`com.example.sound.Domain` содержит:

- модели `Song`, `QueueItem`, `PlaybackQueueState`, `Genre`;
- repository interfaces;
- use cases, в которых есть самостоятельная policy или transformation.

Domain не импортирует Android, Room, Media3, Compose, Data или Presentation. URI хранятся как строки, чтобы модели оставались Android-независимыми.

Flow допустим в repository contracts, поскольку реактивное состояние является частью текущего domain API. Это осознанная практическая зависимость, а не полностью platform-neutral domain.

### Data

`com.example.sound.Data` содержит:

- MediaStore provider;
- Room database, DAO и entities;
- внутреннее файловое хранилище обложек;
- entity/domain mappings;
- реализации domain repositories;
- Hilt bindings и providers.

Data зависит от Domain. Entity, Cursor, Room и Android `Context` не должны выходить через domain contracts.

### Playback service

`com.example.sound.service` — отдельный runtime subsystem внутри того же app process:

- `PlaybackService` владеет ExoPlayer и MediaSession;
- `PlaybackQueueSynchronizer` преобразует сохранённый playback snapshot в playlist Media3;
- `HandleMediaItemTransition` сохраняет фактический переход плеера;
- `MediaItemMapper` преобразует domain models в Media3 и обратно.

Service зависит от Domain и получает repository interfaces через Hilt. Он не обращается к DAO напрямую.

## Допустимые зависимости

Допустимые направления:

```text
root entry points -> Presentation
root entry points -> Data initialization
Presentation      -> Domain
Presentation      -> service boundary для MediaController
service           -> Domain
Data              -> Domain
```

Недопустимые направления для нового кода:

```text
Domain -> Android/Data/Presentation/service
Data -> Presentation
service -> Presentation
UI components -> Room/MediaStore/DAO
ViewModel -> concrete Data repository
```

Use case не добавляется для механического вызова одного repository method. В актуальном подходе простые команды вызывают repository напрямую, а use case сохраняется для логики вроде локального перемещения queue item или преобразования списка в устойчивый порядок IDs.

## Источники истины

### Библиотека песен

Текущий список песен формируется из двух источников:

1. MediaStore предоставляет оригинальные файлы и metadata.
2. Room хранит пользовательские metadata overrides.

`SongRepositoryImpl` объединяет их по `Song.id` и публикует singleton `StateFlow<List<Song>>`. Значение edit overlay заменяет оригинальную MediaStore-модель целиком для совпавшего ID.

### Playback state

Room хранит:

- текущую песню;
- explicit queue;
- default queue snapshot;
- данные, нужные для восстановления проигрывания.

`PlaybackQueueStateRepositoryImpl` строит согласованный `PlaybackQueueState` внутри Room transaction и объединяет его с текущим списком песен.

ExoPlayer не является долговременным источником истины. Он синхронизируется из repository snapshot, а фактические media transitions записываются обратно в Room.

### UI state

ViewModel владеет screen state. Compose владеет только краткоживущим визуальным состоянием, например текущей позицией slider во время drag или состоянием раскрытия dropdown.

Player использует единый immutable `PlayerUiState`. В старых feature state может быть разделён на несколько `StateFlow`; для нового stateful UI предпочтителен один связный immutable state, если поля изменяются как единое состояние.

## Поток загрузки библиотеки

```text
MainActivity permission result
        -> MainActivityViewModel.loadSongs()
        -> SongRepositoryImpl.loadSongs()
        -> combine(MediaStoreSongProvider.observeSongs(), EditSongDao.observeEditSong())
        -> SongRepository.songs StateFlow
        -> MainViewModel / AppNavHostViewModel
        -> Compose UI
```

`MediaStoreSongProvider`:

- регистрирует `ContentObserver` через `callbackFlow`;
- снимает observer в `awaitClose`;
- объединяет external volumes;
- дедуплицирует песни по canonical content URI;
- читает необязательные metadata безопасно;
- выполняет запросы на IO dispatcher.

`SongRepositoryImpl.loadSongs()` идемпотентен: повторный вызов не запускает второй активный collector.

## Поток запуска и перехода playback

```text
Compose event
   -> PlayerViewModel
   -> PlaybackTransitionRepository.startPlayback()
   -> Room transaction
   -> PlaybackQueueStateRepository Flow
   -> PlaybackService
   -> PlaybackQueueSynchronizer
   -> ExoPlayer / MediaSession
   -> PlayerController listener
   -> PlayerUiState
   -> Compose
```

При переходе Media3:

```text
Player.Listener.onMediaItemTransition()
   -> Channel<MediaItem>
   -> sequential consumer
   -> HandleMediaItemTransition
   -> PlaybackTransitionRepository.saveTransition()
   -> Room transaction
```

Последовательный channel consumer нужен, чтобы соседние transitions не записывались в обратном порядке. Queue item ID переносится в `MediaMetadata.extras` и позволяет атомарно удалить проигранный explicit queue item.

## Синхронизация очереди

Логическая очередь состоит из:

1. текущей песни;
2. explicit queue, созданной командами «следующей» и «добавить в очередь»;
3. default library queue, циклически продолженной после текущей песни.

`PlaybackQueueSynchronizer` различает:

- первый запуск или пустой player;
- выбор новой текущей песни;
- изменение metadata текущей песни;
- изменение только upcoming items.

При неизменной текущей песне player не пересоздаётся полностью: текущий MediaItem заменяется, уже проигранные элементы удаляются, upcoming items обновляются после текущего индекса.

## Редактирование metadata

Редактирование не изменяет теги исходного аудиофайла.

```text
EditSongScreen
   -> assisted EditSongViewModel(Song)
   -> ImageRepository для выбранной обложки
   -> PlaybackTransitionRepository.saveInformationEditSong()
   -> EditSongEntity + GenreEntity в Room transaction
   -> SongRepository merge
   -> UI и playback snapshot
```

Обложка копируется во внутренний каталог приложения. `ImageStorage` вычисляет SHA-256 содержимого и использует Room-таблицу путей для дедупликации.

## Compose и state management

Основной UI построен на Material 3 и одном Activity.

Предпочтительная форма feature UI:

```text
Route/Screen composable
   - получает ViewModel
   - собирает Flow lifecycle-aware
   - передаёт значения и callbacks

Content/View composable
   - не знает о Hilt/Room/Media3
   - принимает Modifier
   - поддерживает Preview
   - хранит только локальное визуальное состояние
```

Наиболее близкие reference implementations: `MainScreen/MainScreenRoute` и `SongQueueScreen/SongQueueView`.

Player UI использует sealed `PlayerUIEvent` и единый `PlayerUiState`. Другие feature используют смесь публичных методов и sealed events; единый глобальный MVI framework в проекте отсутствует.

Пользовательский текст должен находиться в string resources. Существующие hardcoded русские строки являются legacy и не задают соглашение для нового UI.

## Navigation

Routes объявлены как сериализуемые типы в sealed interface `Routes`.

```text
MainGraph
|-- SongsRoute
|-- AlbumsRoute
`-- QueueRoute

Top-level destinations
|-- SongPageRoute
|-- SongEditRoute(songId)
`-- SongBottomSheet(songId)
```

`AppUi` владеет `NavHostController`, общим player ViewModel, compact player и bottom navigation. Bottom bar отображается только внутри main graph и скрывается, пока Song Page остаётся видимой.

Route переносит стабильный `songId`, а destination разрешает актуальную `Song` через repository state. `backStackEntry.toRoute<T>()` обязан использовать ровно тот тип, под которым destination зарегистрирован.

## Dependency injection

Hilt composition roots:

- `SoundApplication` — `@HiltAndroidApp`;
- `MainActivity` и `PlaybackService` — `@AndroidEntryPoint`;
- `DatabaseModule` — Room database, DAO и database initializer;
- `RepositoryModule` — singleton bindings domain repository interfaces;
- feature ViewModel — constructor/assisted injection.

`PlaybackQueueSynchronizer` создаётся вручную, потому что зависит от runtime instance ExoPlayer. Остальные зависимости с устойчивым lifecycle создаются Hilt.

В новом коде следует использовать один injection namespace последовательно. Смешение `javax.inject` и `jakarta.inject` в текущем дереве является техническим долгом.

## Coroutines и Flow ownership

Владельцы долгоживущей работы:

- ViewModel — `viewModelScope`;
- `PlaybackService` — собственный `SupervisorJob + Main.immediate`, отменяемый в `onDestroy`;
- singleton `SongRepositoryImpl` — process-lifetime scope;
- `SoundApplication` — одноразовая IO-инициализация базы;
- Compose — lifecycle effects и `rememberCoroutineScope` только для UI interaction.

Ошибки отдельного media transition не завершают service consumer. Cancellation всегда должна распространяться дальше. Room multi-step changes выполняются транзакционно.

Ручное копирование одного Flow в новый `MutableStateFlow` встречается в старом коде. Для нового кода предпочтительны прямое exposure, `stateIn` или осмысленный derived state, если они сохраняют требуемый lifecycle и sharing semantics.

## Хранилище Room

Database version: 1. Схема экспортируется в `app/schemas`.

Таблицы:

| Таблица | Назначение |
|---|---|
| `queue_items` | Explicit playback queue и устойчивый порядок |
| `player_state` | Текущая песня |
| `defaultQueue_items` | Snapshot default queue |
| `editSong_items` | Пользовательские metadata overrides и старые значения |
| `genre_names` | Системные и пользовательские жанры |
| `image_storage` | SHA-256 и внутренний путь сохранённой обложки |

Song snapshot денормализован в нескольких entities. Это позволяет восстановить playback без связей Room, но требует транзакций и явных mappings для согласованности.

## Gradle и сборка

- один Android application module;
- compile/target SDK 36, min SDK 24;
- Java 11;
- Compose compiler plugin и Kotlin serialization;
- KSP для Room и Hilt;
- Room schema export;
- release minification и resource shrinking.

Version catalog используется частично. Прямые версии, дублирующиеся и нецелевые зависимости в production configuration, а также machine-specific proxy/JBR settings в tracked `gradle.properties` являются текущим техническим долгом, а не рекомендуемым соглашением.

Release сейчас подписывается debug signing configuration и не должен считаться production release pipeline.

## Актуальные reference implementations

Использовать как ориентир:

- `Data/provider/MediaStoreSongProvider.kt` — Android callback API как корректно закрываемый Flow;
- `Data/repository/PlaybackQueueStateRepositoryImpl.kt` — согласованный aggregate snapshot;
- `Data/repository/PlaybackTransitionRepositoryImpl.kt` — транзакционные playback transitions;
- `service/PlaybackService.kt` — lifecycle-owned MediaSession/ExoPlayer;
- `service/playback/PlaybackQueueSynchronizer.kt` — изолированная queue policy;
- `Presentation/playerUi/PlayerUiState.kt` — единый immutable UI state;
- `Presentation/playerUi/viewModel/PlayerController.kt` — Media3 adapter boundary;
- `Presentation/songQueue/SongQueueScreen.kt` — stateful wrapper и stateless content;
- `Domain/useCase/queue/MoveQueueItemUseCase.kt` — чистая domain transformation.

## Legacy и известные риски

Не копировать в новый код:

- package names с заглавными буквами и опечатки `defualtQueue`, `utills`;
- несовпадение физического каталога и package;
- `FakeSongProvider` в production source set;
- неиспользуемый domain wrapper `PlayerState`;
- mutable UI model `SortButtonValue`;
- hardcoded пользовательские строки;
- ViewModel, который только копирует один repository Flow в другой Flow без derived state;
- use case как однострочный proxy.

Архитектурно значимые текущие риски:

- пустая MediaStore library воспринимается bootstrap state как бесконечная загрузка;
- `SongEditRoute` декодируется в `AppNavHost` через неправильный route type;
- edit save запускает вложенную ViewModel coroutine перед немедленным pop и может быть отменён вместе с ViewModel;
- default queue записывается в Room, но aggregate repository использует текущий список песен вместо чтения сохранённого порядка;
- `PlayerControllerProvider` и binding module находятся в незавершённом незакоммиченном наборе изменений;
- собственные repository/application scopes не имеют внедряемого dispatcher/lifecycle abstraction.
