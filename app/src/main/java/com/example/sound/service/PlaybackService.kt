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
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.DefaultQueueRepository
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.service.playback.PlaybackQueueObserver
import com.example.sound.service.playback.PlaybackQueueSynchronizer
import com.example.sound.service.playback.SavePlayerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer


    private lateinit var playbackQueueSynchronizer: PlaybackQueueSynchronizer

    @Inject
    lateinit var playbackQueueObserver: PlaybackQueueObserver

    @Inject
    lateinit var playerQueueRepository: PlayerQueueRepository

    @Inject
    lateinit var defaultQueueRepository: DefaultQueueRepository

    @Inject
    lateinit var savePlayerStateFactory: SavePlayerState.Factory
    private lateinit var savePlayerState: SavePlayerState
    private var mediaSession: MediaSession? = null

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private val playerListener = object : Player.Listener {

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int
        ) {
            // Пользователь переключил песню, в том числе
            // через системный плеер или Bluetooth.
            serviceScope.launch {
                savePlayerState()
            }
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
            if (
                playbackState == Player.STATE_ENDED ||
                playbackState == Player.STATE_IDLE
            ) {

            }
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
        savePlayerState = savePlayerStateFactory.create(player)
        buildQueue()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        serviceScope.cancel()
        player.removeListener(playerListener)
        mediaSession?.release()
        mediaSession = null
        player.release()

        super.onDestroy()
    }

    private fun buildQueue() {
        playbackQueueObserver.observe().onEach { state ->
            playbackQueueSynchronizer.synchronizePlayerQueue(state)
        }
            .catch { error ->
                Log.e(TAG, "Queue observation error", error)
            }.launchIn(serviceScope)
    }
    private val TAG = "PlaybackService"
}

data class PlaybackQueueState(
    val currentSong: Song?,
    val queueSongs: List<Song>,
    val defaultQueueSongs: List<Song>,
)

