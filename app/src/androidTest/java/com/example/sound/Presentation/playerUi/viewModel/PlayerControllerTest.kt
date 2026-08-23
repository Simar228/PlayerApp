package com.example.sound.Presentation.playerUi.viewModel

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.test.utils.DummyMainThread
import androidx.media3.test.utils.FakePlayer
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlayerControllerTest {

    private lateinit var fakePlayer: FakePlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var sut: PlayerController

    private lateinit var fakePlayerMainThread: DummyMainThread

    private val instrumentation =
        InstrumentationRegistry.getInstrumentation()

    @Before
    fun setUp() {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        /*
         * FakePlayer должен создаваться на его DummyMainThread.
         */
        fakePlayerMainThread = DummyMainThread()

        fakePlayerMainThread.runOnMainThread {

            val mediaItem = MediaItem.Builder()
                .setMediaId("test-media")
                .setUri("https://example.com/test.mp3")
                .build()

            val mediaItemData =
                SimpleBasePlayer.MediaItemData.Builder(
                    mediaItem.mediaId
                )
                    .setMediaItem(mediaItem)
                    .setMediaMetadata(mediaItem.mediaMetadata)
                    .build()

            fakePlayer = FakePlayer(
                playbackState = Player.STATE_READY,
                playWhenReady = false,
                playlist = listOf(mediaItemData),
                playbackSpeed = 1f,
                bufferingDelayMs = 0L
            )
        }

        /*
         * MediaSession создаём на Android main thread.
         */
        instrumentation.runOnMainSync {
            mediaSession = MediaSession.Builder(
                context,
                fakePlayer
            ).build()
        }

        /*
         * MediaController также создаём для этого MediaSession.
         */
        controllerFuture =
            MediaController.Builder(
                context,
                mediaSession.token
            ).buildAsync()

        /*
         * Ждём, пока MediaController реально подключится.
         */
        val controller = controllerFuture.get()

        assertThat(controller.isConnected).isTrue()

        sut = PlayerController(
            controllerListenerExecutor = MoreExecutors.directExecutor(),
            controllerFuture = controllerFuture,
            onControllerReady = {}
        )

        sut.connect()

        /*
         * Даём PlayerController получить controller.
         */
        controllerFuture.get()
    }

    @After
    fun tearDown() {

        /*
         * ВАЖНО:
         *
         * PlayerController.release()
         * вызывает MediaController.releaseFuture().
         *
         * MediaController требует Android application/main thread.
         *
         * Поэтому sut.release() нельзя вызывать непосредственно
         * из JUnit test thread.
         */
        if (::sut.isInitialized) {
            instrumentation.runOnMainSync {
                sut.release()
            }
        }

        /*
         * MediaSession также освобождаем на main thread.
         */
        if (::mediaSession.isInitialized) {
            instrumentation.runOnMainSync {
                mediaSession.release()
            }
        }

        /*
         * FakePlayer создан на DummyMainThread,
         * поэтому освобождаем его там же.
         */
        if (::fakePlayer.isInitialized) {
            fakePlayerMainThread.runOnMainThread {
                fakePlayer.release()
            }
        }

        if (::fakePlayerMainThread.isInitialized) {
            fakePlayerMainThread.release()
        }
    }

    @Test
    fun onIsPlayingChangedShouldUpdateIsPlayingFieldInStateFlow() = runTest {
        fakePlayerMainThread.runOnMainThread {
            fakePlayer.setPlayWhenReady(true)
        }

        val isPlaying = sut.mediaControllerState.first { it.isPlaying }

        assertThat(isPlaying.isPlaying).isTrue()
    }
}