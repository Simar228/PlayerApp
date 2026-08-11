package com.example.sound.Presentation.playerUi

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import com.example.sound.service.PlaybackService
import com.example.sound.service.playback.toSong
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel internal constructor(
    private val playbackTransitionRepository: PlaybackTransitionRepository,
    private val controllerFuture: ListenableFuture<MediaController>,
    private val controllerListenerExecutor: Executor,
) : ViewModel() {

    @Inject
    constructor(
        playbackTransitionRepository: PlaybackTransitionRepository,
        @ApplicationContext context: Context
    ) : this(
        playbackTransitionRepository = playbackTransitionRepository,
        controllerFuture = createControllerFuture(context),
        controllerListenerExecutor = ContextCompat.getMainExecutor(context)
    )


    private var pendingPlaybackRequest: PendingPlaybackRequest? = null
    private val _connectionState = MutableStateFlow<PlayerConnectionState>(
        PlayerConnectionState.Connecting
    )
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState = _uiState.asStateFlow()
    val connectionState = _connectionState.asStateFlow()
    private var positionUpdatesJob: Job? = null
    private var controller: MediaController? = null
    private val playerListener = object : Player.Listener {

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            _uiState.update { state ->
                state.copy(
                    currentPosition = newPosition.positionMs.coerceAtLeast(0L)
                )
            }
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?, reason: Int
        ) {
            val transitionedSong = mediaItem?.toSong()
            _uiState.update { state ->
                state.copy(
                    currentSong = transitionedSong,
                    currentPosition = 0L,
                    duration = transitionedSong?.duration ?: C.TIME_UNSET,
                )
            }
        }


        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { state ->
                state.copy(
                    isPlaying = isPlaying
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _uiState.update { state ->
                    state.copy(
                        duration = controller?.duration ?: C.TIME_UNSET,
                    )
                }
            }
            if (playbackState == Player.STATE_ENDED) {
                _uiState.update { state ->
                    state.copy(
                        currentPosition = state.duration
                    )
                }
            }
        }
    }

    init {
        Log.d(TAG, "ViewModelStarted")
        controllerFuture.addListener(
            {
                try {
                    val mediaController = controllerFuture.get()

                    controller = mediaController
                    mediaController.addListener(playerListener)

                    synchronizeWithController(mediaController)

                    _connectionState.value = PlayerConnectionState.Ready

                    val pendingRequest = pendingPlaybackRequest
                    pendingPlaybackRequest = null
                    pendingRequest?.let { request ->
                        showSelectedSong(request.selectedSong)

                        playSong(
                            queueSongs = request.queueSongs,
                            selectedSong = request.selectedSong,
                            queueItemId = request.queueItemId
                        )
                    }
                    Log.d(TAG, "MediaController is ready")


                } catch (error: Exception) {
                    controller = null
                    Log.e(TAG, "Error connecting MediaController", error)
                    _connectionState.value = PlayerConnectionState.Error(error)
                    _uiState.value = PlayerUiState()
                }
            }, controllerListenerExecutor
        )
    }

    fun sendSong(
        queueSongs: List<Song> = emptyList(),
        song: Song,
        queueItemId: Long? = null
    ) {
        when (_connectionState.value) {
            PlayerConnectionState.Connecting -> {
                showSelectedSong(song)
                pendingPlaybackRequest = PendingPlaybackRequest(
                    queueSongs = queueSongs,
                    selectedSong = song,
                    queueItemId = queueItemId
                )
                Log.d(
                    TAG,
                    pendingPlaybackRequest.toString()
                )
            }

            PlayerConnectionState.Ready -> {
                playSong(
                    queueSongs = queueSongs,
                    selectedSong = song,
                    queueItemId = queueItemId
                )
            }

            is PlayerConnectionState.Error -> {
                Log.w(
                    TAG,
                    "Cannot play song: controller connection failed"
                )
            }
        }
    }

    private fun playSong(
        queueSongs: List<Song>,
        selectedSong: Song,
        queueItemId: Long?
    ) {
        Log.d(TAG, "Get song: ${selectedSong.title}")
        viewModelScope.launch {
            playbackTransitionRepository.startPlayback(
                song = selectedSong,
                defaultQueueSongs = queueSongs,
                queueItemId = queueItemId
            )
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
                _uiState.update { state ->
                    state.copy(
                        currentPosition = event.positionMs,
                    )
                }
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
                    _uiState.update { state ->
                        state.copy(
                            currentPosition = mediaController.currentPosition.coerceAtLeast(0L)
                        )
                    }
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
                context, ComponentName(context, PlaybackService::class.java)
            )

            return MediaController.Builder(context, sessionToken).buildAsync()
        }
    }

    private fun synchronizeWithController(
        mediaController: MediaController
    ) {
        _uiState.update { state ->
            state.copy(
                currentPosition = mediaController.currentPosition.coerceAtLeast(0L),
                currentSong = mediaController.currentMediaItem?.toSong(),
                duration = mediaController.duration,
                isPlaying = mediaController.isPlaying
            )
        }
    }

    private fun showSelectedSong(song: Song) {
        _uiState.update { state ->
            state.copy(
                currentPosition = 0L,
                currentSong = song,
                duration = song.duration,
            )
        }
    }
}


private data class PendingPlaybackRequest(
    val queueSongs: List<Song>,
    val selectedSong: Song,
    val queueItemId: Long?
)


const val TAG = "PlayerViewModel"
