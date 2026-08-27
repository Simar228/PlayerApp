package com.example.sound.service

import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sound.Domain.repository.HistoryQueueRepository
import com.example.sound.Domain.repository.PlaybackQueueStateRepository
import com.example.sound.service.playback.HandleMediaItemTransition
import com.example.sound.service.playback.PlaybackQueueSynchronizer
import com.example.sound.service.playback.toSong
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var currentMediaItem: MediaItem? = null
    private lateinit var player: ExoPlayer

    private val savedItemToHistory = Channel<MediaItem>(capacity = Channel.UNLIMITED)
    private val mediaTransitionEvents =
        Channel<MediaItem>(capacity = Channel.UNLIMITED)
    private lateinit var playbackQueueSynchronizer: PlaybackQueueSynchronizer

    @Inject
    lateinit var playbackQueueStateRepository: PlaybackQueueStateRepository

    @Inject
    lateinit var historyQueueRepository: HistoryQueueRepository

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
            mediaTransitionEvents.trySend(transitionedMediaItem)
            currentMediaItem?.let { mediaItem ->
                savedItemToHistory.trySend(mediaItem)
            }
            currentMediaItem = transitionedMediaItem
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
            processMediaTransitionEvents()
        }
        serviceScope.launch {
            processMediaItemSaveToHistory()
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


    private suspend fun processMediaItemSaveToHistory() {
        for (mediaItem in savedItemToHistory) {
            try {
                historyQueueRepository.addHistoryItem(
                    mediaItem.toSong(),
                    System.currentTimeMillis()
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(
                    TAG,
                    "Failed to save media item in history",
                    error
                )
            }
        }
    }

    private suspend fun processMediaTransitionEvents() {
        for (mediaItem in mediaTransitionEvents) {
            try {
                handleMediaItemTransition(mediaItem)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.e(
                    TAG,
                    "Failed to handle media item transition",
                    error
                )
            }
        }
    }

    private fun buildQueue() {
        playbackQueueStateRepository.observePlaybackQueueState().onEach { state ->
            playbackQueueSynchronizer.synchronizePlayerQueue(state)
        }
            .retryWhen { error, attempt ->
                Log.e(
                    TAG,
                    "Queue observation failed, retry attempt ${attempt + 1}",
                    error
                )

                delay(1_000L)
                true
            }.launchIn(serviceScope)
    }

    private val TAG = "PlaybackService"
}

