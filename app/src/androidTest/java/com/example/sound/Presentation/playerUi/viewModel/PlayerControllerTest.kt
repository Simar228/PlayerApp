package com.example.sound.Presentation.playerUi.viewModel

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.test.utils.DummyMainThread
import androidx.media3.test.utils.FakePlayer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.playerUi.PlayerConnectionState
import com.example.sound.Presentation.playerUi.PlayerUiState
import com.example.sound.service.playback.toMediaItem
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class PlayerControllerTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val readyCallbackCount = AtomicInteger()

    private lateinit var fakePlayerMainThread: DummyMainThread
    private lateinit var fakePlayer: FakePlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var sut: PlayerController

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fakePlayerMainThread = DummyMainThread()
        fakePlayerMainThread.runOnMainThread {
            fakePlayer = FakePlayer(
                playbackState = Player.STATE_READY,
                playWhenReady = false,
                playlist = listOf(
                    FakeSong.SONG_1.toMediaItemData(),
                    FakeSong.SONG_2.toMediaItemData(),
                ),
                bufferingDelayMs = 0L,
            )
            fakePlayer.setPosition(INITIAL_POSITION_MS)
        }

        instrumentation.runOnMainSync {
            mediaSession = MediaSession.Builder(context, fakePlayer).build()
        }
        controllerFuture = MediaController.Builder(context, mediaSession.token).buildAsync()
        assertThat(controllerFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS).isConnected).isTrue()

        sut = PlayerController(
            controllerListenerExecutor = MoreExecutors.directExecutor(),
            controllerFuture = controllerFuture,
            onControllerReady = { readyCallbackCount.incrementAndGet() },
        )
        instrumentation.runOnMainSync { sut.connect() }
        awaitState { it.connectionState == PlayerConnectionState.Ready }
    }

    @After
    fun tearDown() {
        if (::sut.isInitialized) {
            instrumentation.runOnMainSync { sut.release() }
        }
        if (::mediaSession.isInitialized) {
            instrumentation.runOnMainSync { mediaSession.release() }
        }
        if (::fakePlayer.isInitialized) {
            fakePlayerMainThread.runOnMainThread { fakePlayer.release() }
        }
        if (::fakePlayerMainThread.isInitialized) {
            fakePlayerMainThread.release()
        }
    }

    @Test
    fun connectSynchronizesControllerStateAndInvokesReadyCallback() {
        assertThat(sut.mediaControllerState.value).isEqualTo(
            PlayerUiState(
                connectionState = PlayerConnectionState.Ready,
                currentSong = FakeSong.SONG_1,
                isPlaying = false,
                currentPosition = INITIAL_POSITION_MS,
                duration = FakeSong.SONG_1.duration,
            )
        )
        assertThat(readyCallbackCount.get()).isEqualTo(1)
    }

    @Test
    fun connectWhenAlreadyConnectedDoesNotInvokeReadyCallbackAgain() {
        instrumentation.runOnMainSync {
            sut.connect()
            sut.connect()
        }

        assertThat(readyCallbackCount.get()).isEqualTo(1)
    }

    @Test
    fun playAndPausePublishActualPlaybackState() {
        instrumentation.runOnMainSync { sut.play() }
        drainMedia3Tasks()

        assertThat(sut.mediaControllerState.value.isPlaying).isTrue()
        assertThat(fakePlayerValue { it.playWhenReady }).isTrue()

        instrumentation.runOnMainSync { sut.pause() }
        drainMedia3Tasks()

        assertThat(sut.mediaControllerState.value.isPlaying).isFalse()
        assertThat(fakePlayerValue { it.playWhenReady }).isFalse()
    }

    @Test
    fun seekToUpdatesPositionAndForwardsSeekToPlayer() {
        instrumentation.runOnMainSync { sut.seekTo(SEEK_POSITION_MS) }
        drainMedia3Tasks()

        assertThat(sut.mediaControllerState.value.currentPosition)
            .isEqualTo(SEEK_POSITION_MS)
        assertThat(fakePlayerValue { it.currentPosition }).isEqualTo(SEEK_POSITION_MS)
    }

    @Test
    fun playerPositionDiscontinuityUpdatesPublishedPosition() {
        fakePlayerMainThread.runOnMainThread {
            fakePlayer.setPosition(EXTERNAL_POSITION_MS)
        }

        assertThat(awaitState { it.currentPosition == EXTERNAL_POSITION_MS }.currentPosition)
            .isEqualTo(EXTERNAL_POSITION_MS)
    }

    @Test
    fun updatePositionReadsControllerPositionWhilePlaying() {
        instrumentation.runOnMainSync { sut.play() }
        awaitState { it.isPlaying }
        fakePlayerMainThread.runOnMainThread {
            fakePlayer.setPosition(EXTERNAL_POSITION_MS)
        }
        awaitState { it.currentPosition >= EXTERNAL_POSITION_MS }
        sut.showSelectedSong(FakeSong.SONG_1)

        instrumentation.runOnMainSync { sut.updatePosition() }

        assertThat(sut.mediaControllerState.value.currentPosition)
            .isAtLeast(EXTERNAL_POSITION_MS)
    }

    @Test
    fun updatePositionDoesNotChangePositionWhilePaused() {
        fakePlayerMainThread.runOnMainThread {
            fakePlayer.setPosition(EXTERNAL_POSITION_MS)
        }
        awaitState { it.currentPosition == EXTERNAL_POSITION_MS }
        sut.showSelectedSong(FakeSong.SONG_1)

        instrumentation.runOnMainSync { sut.updatePosition() }

        assertThat(sut.mediaControllerState.value.currentPosition).isEqualTo(0L)
    }

    @Test
    fun nextPublishesTransitionedSong() {
        instrumentation.runOnMainSync { sut.next() }
        drainMedia3Tasks()
        val nextState = sut.mediaControllerState.value

        assertThat(fakePlayerValue { it.currentMediaItemIndex }).isEqualTo(1)
        assertThat(nextState.currentSong).isEqualTo(FakeSong.SONG_2)
        assertThat(nextState.currentPosition).isEqualTo(0L)
        assertThat(nextState.duration).isEqualTo(FakeSong.SONG_2.duration)
    }

    @Test
    fun previousPublishesTransitionedSong() {
        instrumentation.runOnMainSync { sut.next() }
        awaitState { it.currentSong == FakeSong.SONG_2 }
        drainMedia3Tasks()

        instrumentation.runOnMainSync { sut.previous() }
        drainMedia3Tasks()

        assertThat(fakePlayerValue { it.currentMediaItemIndex }).isEqualTo(0)
        assertThat(sut.mediaControllerState.value.currentSong).isEqualTo(FakeSong.SONG_1)
    }

    @Test
    fun readyPlaybackStateRefreshesDurationFromController() {
        fakePlayerMainThread.runOnMainThread {
            fakePlayer.setPlaybackState(Player.STATE_BUFFERING)
        }
        drainMedia3Tasks()
        fakePlayerMainThread.runOnMainThread {
            fakePlayer.setDuration(FakeSong.SONG_1.id, UPDATED_DURATION_MS)
        }
        drainMedia3Tasks()
        fakePlayerMainThread.runOnMainThread {
            fakePlayer.setPlaybackState(Player.STATE_READY)
        }

        assertThat(awaitState { it.duration == UPDATED_DURATION_MS }.duration)
            .isEqualTo(UPDATED_DURATION_MS)
    }

    @Test
    fun endedPlaybackStateMovesPositionToDuration() {
        fakePlayerMainThread.runOnMainThread {
            fakePlayer.setPlaybackState(Player.STATE_ENDED)
        }

        val state = awaitState { it.currentPosition == FakeSong.SONG_1.duration }
        assertThat(state.currentPosition).isEqualTo(state.duration)
    }

    @Test
    fun showSelectedSongPublishesOptimisticSongState() {
        sut.showSelectedSong(FakeSong.SONG_2)

        assertThat(sut.mediaControllerState.value.currentSong).isEqualTo(FakeSong.SONG_2)
        assertThat(sut.mediaControllerState.value.currentPosition).isEqualTo(0L)
        assertThat(sut.mediaControllerState.value.duration).isEqualTo(FakeSong.SONG_2.duration)
    }

    @Test
    fun releaseIgnoresLaterCommandsAndReconnect() {
        instrumentation.runOnMainSync { sut.play() }
        awaitState { it.isPlaying }

        instrumentation.runOnMainSync {
            sut.release()
            sut.pause()
            sut.connect()
        }
        drainMedia3Tasks()

        assertThat(fakePlayerValue { it.playWhenReady }).isTrue()
        assertThat(readyCallbackCount.get()).isEqualTo(1)
    }

    @Test
    fun releaseStopsPublishingPlayerCallbacks() {
        instrumentation.runOnMainSync { sut.release() }

        val releasedState = sut.mediaControllerState.value
        fakePlayerMainThread.runOnMainThread {
            fakePlayer.setPlayWhenReady(true)
            fakePlayer.setPosition(EXTERNAL_POSITION_MS)
        }
        drainMedia3Tasks()

        assertThat(sut.mediaControllerState.value).isEqualTo(releasedState)
    }

    @Test
    fun releaseCanBeCalledMoreThanOnce() {
        val stateBeforeRelease = sut.mediaControllerState.value

        instrumentation.runOnMainSync {
            sut.release()
            sut.release()
        }

        assertThat(sut.mediaControllerState.value).isEqualTo(stateBeforeRelease)
    }

    @Test
    fun releaseWhileConnectionIsPendingCancelsConnectionAndSkipsReadyCallback() {
        val pendingFuture = SettableFuture.create<MediaController>()
        val callbackCount = AtomicInteger()
        val pendingController = PlayerController(
            controllerListenerExecutor = MoreExecutors.directExecutor(),
            controllerFuture = pendingFuture,
            onControllerReady = { callbackCount.incrementAndGet() },
        )

        instrumentation.runOnMainSync {
            pendingController.connect()
            pendingController.release()
        }

        assertThat(pendingFuture.isCancelled).isTrue()
        assertThat(callbackCount.get()).isEqualTo(0)
    }

    @Test
    fun failedConnectionPublishesErrorAndDoesNotInvokeReadyCallback() {
        val expectedError = IllegalStateException("connection failed")
        val callbackCount = AtomicInteger()
        val failedController = PlayerController(
            controllerListenerExecutor = MoreExecutors.directExecutor(),
            controllerFuture = Futures.immediateFailedFuture(expectedError),
            onControllerReady = { callbackCount.incrementAndGet() },
        )

        instrumentation.runOnMainSync { failedController.connect() }

        val connectionState = failedController.mediaControllerState.value.connectionState
        assertThat(connectionState).isInstanceOf(PlayerConnectionState.Error::class.java)
        assertThat((connectionState as PlayerConnectionState.Error).cause)
            .hasCauseThat()
            .isSameInstanceAs(expectedError)
        assertThat(callbackCount.get()).isEqualTo(0)
        instrumentation.runOnMainSync { failedController.release() }
    }

    private fun awaitState(
        predicate: (PlayerUiState) -> Boolean,
    ): PlayerUiState = runBlocking {
        withTimeout(TIMEOUT_MS) {
            sut.mediaControllerState.first(predicate)
        }
    }

    private fun drainMedia3Tasks() {
        instrumentation.waitForIdleSync()
        fakePlayerMainThread.runOnMainThread {}
        instrumentation.waitForIdleSync()
    }

    private fun <T> fakePlayerValue(value: (FakePlayer) -> T): T {
        lateinit var result: Any
        fakePlayerMainThread.runOnMainThread {
            result = value(fakePlayer) as Any
        }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    private fun Song.toMediaItemData(): SimpleBasePlayer.MediaItemData {
        val mediaItem = toMediaItem()
        return SimpleBasePlayer.MediaItemData.Builder(id)
            .setMediaItem(mediaItem)
            .setMediaMetadata(mediaItem.mediaMetadata)
            .setDurationUs(duration * 1_000)
            .setIsSeekable(true)
            .build()
    }

    private companion object {
        const val INITIAL_POSITION_MS = 1_234L
        const val SEEK_POSITION_MS = 4_321L
        const val EXTERNAL_POSITION_MS = 6_789L
        const val UPDATED_DURATION_MS = 15_000L
        const val TIMEOUT_MS = 5_000L

    }
}
