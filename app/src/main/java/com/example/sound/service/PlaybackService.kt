package com.example.sound.service

import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sound.Domain.repository.PlaybackQueueStateRepository
import com.example.sound.service.playback.HandleMediaItemTransition
import com.example.sound.service.playback.PlaybackQueueSynchronizer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer

    private val mediaTransitionEvents =
        Channel<MediaItem>(capacity = Channel.UNLIMITED)
    private lateinit var playbackQueueSynchronizer: PlaybackQueueSynchronizer

    @Inject
    lateinit var playbackQueueStateRepository: PlaybackQueueStateRepository

    @Inject
    lateinit var handleMediaItemTransition: HandleMediaItemTransition
    private var mediaSession: MediaSession? = null

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private val playerListener = object : Player.Listener {

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int
        ) {
            val transitionedMediaItem = mediaItem ?: return
            // Пользователь переключил песню, в том числе
            // через системный плеер или Bluetooth.
            mediaTransitionEvents.trySend(transitionedMediaItem)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // Play или Pause.

        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            // Перемотка или переход на другой элемент.

        }

        override fun onPlaybackStateChanged(playbackState: Int) {
        }

        override fun onTimelineChanged(
            timeline: Timeline,
            reason: Int
        ) {
            // Изменилась очередь плеера.

        }
    }

    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                setHandleAudioBecomingNoisy(true)
                addListener(playerListener)
            }
        mediaSession = MediaSession.Builder(this, player)
            .build()
        playbackQueueSynchronizer = PlaybackQueueSynchronizer(player)
        buildQueue()

        serviceScope.launch {
            for (mediaItem in mediaTransitionEvents) {
                handleMediaItemTransition(mediaItem)
            }
        }
    }

    override fun onDestroy() {
        mediaTransitionEvents.close()
        serviceScope.cancel()
        player.removeListener(playerListener)
        mediaSession?.release()
        mediaSession = null
        player.release()

        super.onDestroy()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }



    private fun buildQueue() {
        playbackQueueStateRepository.observePlaybackQueueState().onEach { state ->
            playbackQueueSynchronizer.synchronizePlayerQueue(state)
        }
            .catch { error ->
                Log.e(TAG, "Queue observation error", error)
            }.launchIn(serviceScope)
    }

    private val TAG = "PlaybackService"
}

