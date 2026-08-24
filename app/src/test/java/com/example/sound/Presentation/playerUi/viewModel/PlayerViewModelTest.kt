package com.example.sound.Presentation.playerUi.viewModel

import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.repository.FakePlaybackTransitionRepository
import com.example.sound.Presentation.playerUi.PlayerConnectionState
import com.example.sound.Presentation.playerUi.PlayerUIEvent
import com.example.sound.Presentation.playerUi.PlayerUiState
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakePlaybackTransitionRepository
    private lateinit var playerController: FakePlayerController
    private lateinit var sut: PlayerViewModel

    @Before
    fun setUp() {
        repository = FakePlaybackTransitionRepository()
        playerController = FakePlayerController()

        val playerControllerProvider = PlayerControllerProvider { onControllerReady ->
            playerController.apply {
                this.onControllerReady = onControllerReady
            }
        }

        sut = PlayerViewModel(repository, playerControllerProvider)
    }

    @Test
    fun `init should immediately call connect on playerController`() {
        assertThat(playerController.connectCalled).isTrue()
    }

    @Test
    fun `sendSong when connection is Connecting should cache request and show song stub`() = runTest {
        playerController.emitState(
            PlayerUiState(connectionState = PlayerConnectionState.Connecting)
        )
        val song = FakeSong.SONG_0

        sut.sendSong(song = song)

        assertThat(playerController.showSelectedSongCalledWith).isEqualTo(song)
        assertThat(repository.startPlaybackCalls).isEmpty()
    }

    @Test
    fun `when controller becomes Ready, any pending playback request must be executed`() = runTest {
        playerController.emitState(
            PlayerUiState(connectionState = PlayerConnectionState.Connecting)
        )
        val song = FakeSong.SONG_1
        sut.sendSong(song = song)

        playerController.triggerReady()
        advanceUntilIdle()

        assertThat(repository.startPlaybackCalls).containsExactly(
            FakePlaybackTransitionRepository.StartPlaybackCall(
                song = song,
                defaultQueueSongs = null,
                queueItemId = null
            )
        )
    }

    @Test
    fun `pending playback request should preserve queue and queue item id`() = runTest {
        playerController.emitState(
            PlayerUiState(connectionState = PlayerConnectionState.Connecting)
        )
        val queueSongs = listOf(FakeSong.SONG_0, FakeSong.SONG_1, FakeSong.SONG_2)

        sut.sendSong(
            queueSongs = queueSongs,
            song = FakeSong.SONG_1,
            queueItemId = 42L
        )
        playerController.triggerReady()
        advanceUntilIdle()

        assertThat(repository.startPlaybackCalls).containsExactly(
            FakePlaybackTransitionRepository.StartPlaybackCall(
                song = FakeSong.SONG_1,
                defaultQueueSongs = queueSongs,
                queueItemId = 42L
            )
        )
    }

    @Test
    fun `pending playback request should be executed only once`() = runTest {
        playerController.emitState(
            PlayerUiState(connectionState = PlayerConnectionState.Connecting)
        )
        sut.sendSong(song = FakeSong.SONG_0)

        playerController.triggerReady()
        playerController.triggerReady()
        advanceUntilIdle()

        assertThat(repository.startPlaybackCalls).containsExactly(
            FakePlaybackTransitionRepository.StartPlaybackCall(
                song = FakeSong.SONG_0,
                defaultQueueSongs = null,
                queueItemId = null
            )
        )
    }

    @Test
    fun `latest pending playback request should replace previous request`() = runTest {
        playerController.emitState(
            PlayerUiState(connectionState = PlayerConnectionState.Connecting)
        )

        sut.sendSong(song = FakeSong.SONG_0)
        sut.sendSong(song = FakeSong.SONG_2)
        playerController.triggerReady()
        advanceUntilIdle()

        assertThat(repository.startPlaybackCalls).containsExactly(
            FakePlaybackTransitionRepository.StartPlaybackCall(
                song = FakeSong.SONG_2,
                defaultQueueSongs = null,
                queueItemId = null
            )
        )
    }

    @Test
    fun `sendSong when connection is Ready should play song instantly`() = runTest {
        playerController.emitState(
            PlayerUiState(connectionState = PlayerConnectionState.Ready)
        )
        val song = FakeSong.SONG_2

        sut.sendSong(song = song)
        advanceUntilIdle()

        assertThat(repository.startPlaybackCalls).containsExactly(
            FakePlaybackTransitionRepository.StartPlaybackCall(song = song, defaultQueueSongs = null, queueItemId = null)
        )
    }

    @Test
    fun `new playback request should cancel unfinished previous request`() = runTest {
        playerController.emitState(
            PlayerUiState(connectionState = PlayerConnectionState.Ready)
        )
        repository.songToSuspend = FakeSong.SONG_0

        try {
            sut.sendSong(song = FakeSong.SONG_0)
            sut.sendSong(song = FakeSong.SONG_1)
            runCurrent()

            assertThat(repository.startPlaybackCalls.map { it.song }).containsExactly(
                FakeSong.SONG_0,
                FakeSong.SONG_1
            ).inOrder()
            assertThat(repository.cancelledPlaybackSongs).containsExactly(FakeSong.SONG_0)
        } finally {
            sut.viewModelScope.cancel()
        }
    }

    @Test
    fun `sendEvent should forward corresponding UI events to playerController`() {
        sut.sendEvent(PlayerUIEvent.NextSong)
        sut.sendEvent(PlayerUIEvent.PreviousSong)
        sut.sendEvent(PlayerUIEvent.Play)
        sut.sendEvent(PlayerUIEvent.Pause)
        sut.sendEvent(PlayerUIEvent.SeekTo(4200L))

        assertThat(playerController.invokedEvents).containsExactly(
            "next",
            "previous",
            "play",
            "pause",
            "seekTo-4200"
        ).inOrder()
    }

    @Test
    fun `startPositionUpdates should poll player position periodically`() = runTest {
        sut.startPositionUpdates()

        advanceTimeBy(510L)

        assertThat(playerController.invokedEvents.count { it == "updatePosition" })
            .isAtLeast(2)
        sut.stopPositionUpdates()
    }

    @Test
    fun `stopPositionUpdates should stop polling player position`() = runTest {
        sut.startPositionUpdates()
        try {
            advanceTimeBy(510L)
            sut.stopPositionUpdates()
            val callsBeforeStop = updatePositionCallCount()

            advanceTimeBy(1_000L)

            assertThat(updatePositionCallCount()).isEqualTo(callsBeforeStop)
        } finally {
            sut.viewModelScope.cancel()
        }
    }

    @Test
    fun `repeated startPositionUpdates should not start another polling job`() = runTest {
        sut.startPositionUpdates()
        try {
            sut.startPositionUpdates()

            advanceTimeBy(510L)

            assertThat(updatePositionCallCount()).isEqualTo(3)
        } finally {
            sut.viewModelScope.cancel()
        }
    }

    @Test
    fun `sendSong when connection is Error should not play song and not update controller`() = runTest {
        playerController.emitState(
            PlayerUiState(
                connectionState = PlayerConnectionState.Error(
                    Exception("Media3 Connection Failed")
                )
            )
        )

        sut.sendSong(song = FakeSong.SONG_0)
        advanceUntilIdle()

        assertThat(repository.startPlaybackCalls).isEmpty()
        assertThat(playerController.showSelectedSongCalledWith).isNull()
    }

    @Test
    fun `ViewModel cleared should stop position updates and release playerController`() = runTest {
        sut.startPositionUpdates()
        try {
            advanceTimeBy(510L)
            val callsBeforeCleared = updatePositionCallCount()

            clearViewModel()
            advanceTimeBy(1_000L)

            assertThat(playerController.releaseCalled).isTrue()
            assertThat(updatePositionCallCount()).isEqualTo(callsBeforeCleared)
        } finally {
            sut.viewModelScope.cancel()
        }
    }

    private fun updatePositionCallCount(): Int =
        playerController.invokedEvents.count { it == "updatePosition" }

    private fun clearViewModel() {
        val method = PlayerViewModel::class.java.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(sut)
    }
}
