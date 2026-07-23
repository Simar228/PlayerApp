package com.example.sound.Presentation.playerUi

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sound.Data.local.defualtQueue.toSong
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.model.toSong
import com.example.sound.Domain.repository.DefaultQueueRepository
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.Domain.repository.PlayerStateRepository
import com.example.sound.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel internal constructor(
    private val playerStateRepository: PlayerStateRepository,
    private val playerQueueRepository: PlayerQueueRepository,
    private val controllerFuture: ListenableFuture<MediaController>,
    private val controllerListenerExecutor: Executor,
    private val defaultQueueRepository: DefaultQueueRepository
) : ViewModel() {

    @Inject
    constructor(
        defaultQueueRepository: DefaultQueueRepository,
        playerStateRepository: PlayerStateRepository,
        playerQueueRepository: PlayerQueueRepository,
        @ApplicationContext context: Context
    ) : this(
        defaultQueueRepository = defaultQueueRepository,
        playerStateRepository = playerStateRepository,
        playerQueueRepository = playerQueueRepository,
        controllerFuture = createControllerFuture(context),
        controllerListenerExecutor = ContextCompat.getMainExecutor(context)
    )


    private var pendingPlaybackRequest: PendingPlaybackRequest? = null
    private val _connectionState =
        MutableStateFlow<PlayerConnectionState>(
            PlayerConnectionState.Connecting
        )
    val connectionState = _connectionState.asStateFlow()
    private var positionUpdatesJob: Job? = null
    private var controller: MediaController? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()
    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()
    private val playerListener = object : Player.Listener {


        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int
        ) {
            _currentSong.value = mediaItem?.toSong()
            _currentPosition.value = 0L
            updateDuration()
        }


        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                updateDuration()
            }
            if (playbackState == Player.STATE_ENDED) {
                _currentPosition.value = _duration.value
            }
        }
    }

    init {
        Log.d(TAG, "ViewModelStarted")
        controllerFuture.addListener(
            {
                try {
                    controller = controllerFuture.get()
                    controller?.addListener(playerListener)
                    _connectionState.value = PlayerConnectionState.Ready
                    val pendingRequest = pendingPlaybackRequest
                    pendingPlaybackRequest = null
                    pendingRequest?.let { request ->
                        playSong(
                            queueSongs = request.queueSongs,
                            selectedSong = request.selectedSong
                        )
                    }
                    Log.d(TAG, "MediaController is ready")


                } catch (error: Exception) {
                    _currentSong.value = null
                    controller = null
                    Log.e(TAG, "Error connecting MediaController", error)
                    _connectionState.value = PlayerConnectionState.Error(error)
                }
            },
            controllerListenerExecutor
        )
    }


    private fun updateDuration() {
        _duration.value = controller?.duration ?: C.TIME_UNSET
    }


    private fun playSong(queueSongs: List<Song>, selectedSong: Song) {
        Log.d(TAG, "Get song: ${selectedSong.title}")
        viewModelScope.launch {
            if (queueSongs.isNotEmpty()){
                defaultQueueRepository.updateDefaultQueue(queueSongs)
            }
            playerStateRepository.setPlayerState(
                PlayerState(
                    currentSong = selectedSong
                )
            )
        }
    }


    fun sendSong(queueSongs: List<Song> = emptyList(), song: Song) {
        when (_connectionState.value) {
            PlayerConnectionState.Connecting -> {
                _currentSong.value = song
                pendingPlaybackRequest = PendingPlaybackRequest(
                    queueSongs = queueSongs,
                    selectedSong = song
                )
                Log.d(TAG, pendingPlaybackRequest.toString())
            }

            PlayerConnectionState.Ready -> {
                playSong(queueSongs, song)
            }

            is PlayerConnectionState.Error -> {
                Log.w(TAG, "Cannot play song: controller connection failed")
            }
        }
    }

    fun sendEvent(event: PlayerUIEvent) {
        when (event) {
            is PlayerUIEvent.Play -> {
                controller?.play()
            }

            is PlayerUIEvent.Pause -> {
                controller?.pause()
            }

            is PlayerUIEvent.SeekTo -> {
                _currentPosition.value = event.positionMs
                controller?.seekTo(event.positionMs)
            }

            is PlayerUIEvent.NextSong -> {
                controller?.seekToNextMediaItem()
            }

            is PlayerUIEvent.PreviousSong -> {
                controller?.seekToPreviousMediaItem()
            }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModelCleared")
        stopPositionUpdates()
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
        controller = null
        super.onCleared()
    }

    fun startPositionUpdates() {
        if (positionUpdatesJob?.isActive == true) return
        positionUpdatesJob = viewModelScope.launch {
            while (isActive) {
                val mediaController = controller
                if (mediaController?.isPlaying == true) {
                    _currentPosition.value = mediaController.currentPosition.coerceAtLeast(0L)
                }
                delay(250L)
            }
        }
    }

    fun stopPositionUpdates() {
        positionUpdatesJob?.cancel()
        positionUpdatesJob = null
    }

    private companion object {
        fun createControllerFuture(context: Context): ListenableFuture<MediaController> {
            val sessionToken = SessionToken(
                context,
                ComponentName(context, PlaybackService::class.java)
            )

            return MediaController.Builder(context, sessionToken)
                .buildAsync()
        }
    }
}

private fun MediaItem.toSong(): Song {
    return Song(
        id = mediaId,
        title = mediaMetadata.title?.toString().orEmpty(),
        artist = mediaMetadata.artist?.toString().orEmpty(),
        duration = mediaMetadata.durationMs?.toLong() ?: 0,
        uri = localConfiguration?.uri
            ?: Uri.EMPTY,
        album = mediaMetadata.albumTitle?.toString().orEmpty(),
        genre = mediaMetadata.genre?.toString().orEmpty(),
        art = mediaMetadata.artworkUri
    )

}

private data class PendingPlaybackRequest(
    val queueSongs: List<Song>,
    val selectedSong: Song
)


private const val EXTRA_DEFAULT_QUEUE = "defaultQueue"
private fun Song.toMediaItem(isDefaultQueue: Boolean): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(art)
                .setDurationMs(duration)
                .setGenre(genre)
                .setAlbumTitle(album)
                .setExtras(
                    Bundle().apply {
                        putBoolean(EXTRA_DEFAULT_QUEUE, isDefaultQueue)
                    }
                )
                .build()
        )
        .build()
}

const val TAG = "PlayerViewModel"
