package com.example.sound.Presentation.playerUi.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import com.example.sound.Presentation.activity.MainDispatcherRule
import com.example.sound.Presentation.playerUi.PlayerConnectionState
import com.example.sound.Presentation.playerUi.PlayerUIEvent
import com.example.sound.Presentation.playerUi.PlayerUiState
import com.example.sound.testing.createTestSong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private lateinit var repository: PlaybackTransitionRepository
    private lateinit var controller: PlayerController
    private lateinit var controllerFactory: PlayerControllerFactory
    private lateinit var controllerState: MutableStateFlow<PlayerUiState>
    private lateinit var onControllerReady: () -> Unit
    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setUp() {
        repository = mock<PlaybackTransitionRepository>()
        controller = mock<PlayerController>()
        controllerFactory = mock<PlayerControllerFactory>()
        controllerState = MutableStateFlow(PlayerUiState())

        whenever(controller.mediaControllerState)
            .thenReturn(controllerState.asStateFlow())
        whenever(controllerFactory.create(any()))
            .thenAnswer { invocation ->
                onControllerReady = invocation.getArgument(0)
                controller
            }

        viewModel = PlayerViewModel(
            playbackTransitionRepository = repository,
            playerControllerFactory = controllerFactory,
        )
    }

    @Test
    fun `play event delegates to player controller`() {
        // When
        viewModel.sendEvent(PlayerUIEvent.Play)

        // Then
        verify(controller).play()
    }

    @Test
    fun `pause event delegates to player controller`() {
        // When
        viewModel.sendEvent(PlayerUIEvent.Pause)

        // Then
        verify(controller).pause()
    }

    @Test
    fun `seek event delegates position to player controller`() {
        // Given
        val positionMs = 12_345L

        // When
        viewModel.sendEvent(PlayerUIEvent.SeekTo(positionMs))

        // Then
        verify(controller).seekTo(positionMs)
    }

    @Test
    fun `next song event delegates to player controller`() {
        // When
        viewModel.sendEvent(PlayerUIEvent.NextSong)

        // Then
        verify(controller).next()
    }

    @Test
    fun `previous song event delegates to player controller`() {
        // When
        viewModel.sendEvent(PlayerUIEvent.PreviousSong)

        // Then
        verify(controller).previous()
    }

    @Test
    fun `position updates stop after stopPositionUpdates`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // When
            viewModel.startPositionUpdates()
            runCurrent()

            // Then: one immediate update
            verify(controller, times(1)).updatePosition()

            // When: 500 ms of virtual time passes
            advanceTimeBy(500L)
            runCurrent()

            // Then: updates happened at 0, 250 and 500 ms
            verify(controller, times(3)).updatePosition()

            // When
            viewModel.stopPositionUpdates()
            advanceTimeBy(1_000L)
            runCurrent()

            // Then: stopping prevents any additional updates
            verify(controller, times(3)).updatePosition()
        }

    @Test
    fun `controller ready without pending request does not start playback`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // When
            makeControllerReady()

            // Then
            verifyNoInteractions(repository)
        }

    @Test
    fun `sendSong while connecting stores request and starts it when controller is ready`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            val selectedSong = createTestSong(id = "selected")
            val queueSongs = listOf(
                createTestSong(id = "A"),
                selectedSong,
            )
            val queueItemId = 42L

            // When: controller is still connecting
            viewModel.sendSong(
                queueSongs = queueSongs,
                song = selectedSong,
                queueItemId = queueItemId,
            )

            // Then: song is shown immediately, but playback is still pending
            verify(controller).showSelectedSong(selectedSong)
            verifyNoInteractions(repository)

            // When: pending request is consumed after connection
            makeControllerReady()
            runCurrent()

            // Then: all pending request fields are passed to the repository
            verify(repository).startPlayback(
                song = selectedSong,
                defaultQueueSongs = queueSongs,
                queueItemId = queueItemId,
            )
        }

    @Test
    fun `sendSong while ready starts playback immediately`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            val selectedSong = createTestSong(id = "selected")
            val queueSongs = listOf(selectedSong)
            val queueItemId = 42L
            controllerState.value = controllerState.value.copy(
                connectionState = PlayerConnectionState.Ready,
            )

            // When
            viewModel.sendSong(
                queueSongs = queueSongs,
                song = selectedSong,
                queueItemId = queueItemId,
            )
            runCurrent()

            // Then
            verify(repository).startPlayback(
                song = selectedSong,
                defaultQueueSongs = queueSongs,
                queueItemId = queueItemId,
            )
        }

    @Test
    fun `sendSong after connection error does not start playback`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            val selectedSong = createTestSong(id = "selected")
            controllerState.value = controllerState.value.copy(
                connectionState = PlayerConnectionState.Error(
                    cause = RuntimeException("Connection failed"),
                ),
            )

            // When
            viewModel.sendSong(song = selectedSong)
            runCurrent()

            // Then
            verifyNoInteractions(repository)
            verify(controller, never()).showSelectedSong(selectedSong)
        }

    @Test
    fun `controller ready starts only latest pending song`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            val songA = createTestSong(id = "A")
            val songB = createTestSong(id = "B")
            viewModel.sendSong(song = songA)
            viewModel.sendSong(song = songB)

            // When
            makeControllerReady()
            runCurrent()

            // Then
            verify(repository).startPlayback(
                song = songB,
                defaultQueueSongs = null,
                queueItemId = null,
            )
            verify(repository, never()).startPlayback(
                song = songA,
                defaultQueueSongs = null,
                queueItemId = null,
            )
        }

    @Test
    fun `pending playback request is consumed only once`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            val song = createTestSong(id = "A")
            viewModel.sendSong(song = song)

            // When: controller reports readiness twice
            makeControllerReady()
            runCurrent()
            onControllerReady()
            runCurrent()

            // Then: cleared pending request cannot be replayed
            verify(repository, times(1)).startPlayback(
                song = song,
                defaultQueueSongs = null,
                queueItemId = null,
            )
        }

    @Test
    fun `new playback request cancels previous queued request`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            val songA = createTestSong(id = "A")
            val songB = createTestSong(id = "B")
            controllerState.value = controllerState.value.copy(
                connectionState = PlayerConnectionState.Ready,
            )

            // When: both launches are queued on StandardTestDispatcher
            viewModel.sendSong(song = songA)
            viewModel.sendSong(song = songB)
            runCurrent()

            // Then: the first job was cancelled before it could start
            verify(repository, never()).startPlayback(
                song = songA,
                defaultQueueSongs = null,
                queueItemId = null,
            )
            verify(repository, times(1)).startPlayback(
                song = songB,
                defaultQueueSongs = null,
                queueItemId = null,
            )
        }

    @Test
    fun `clearing ViewModel releases player controller`() {
        // Given: ViewModelStore owns this ViewModel and can call onCleared()
        val viewModelStore = ViewModelStore()
        ViewModelProvider(
            viewModelStore,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlayerViewModel(
                        playbackTransitionRepository = repository,
                        playerControllerFactory = controllerFactory,
                    ) as T
                }
            },
        )[PlayerViewModel::class.java]

        // When
        viewModelStore.clear()

        // Then
        verify(controller, times(1)).release()
    }

    @Test
    fun `startPositionUpdates does not start duplicate job`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.startPositionUpdates()
            viewModel.startPositionUpdates()
            runCurrent()

            verify(controller, times(1)).updatePosition()

            advanceTimeBy(250L)
            runCurrent()

            verify(controller, times(2)).updatePosition()
            viewModel.stopPositionUpdates()
        }

    @Test
    fun `initialization connects player controller once`() {
        verify(controller, times(1)).connect()
    }

    private fun makeControllerReady() {
        controllerState.value = controllerState.value.copy(
            connectionState = PlayerConnectionState.Ready,
        )
        onControllerReady()
    }
}
