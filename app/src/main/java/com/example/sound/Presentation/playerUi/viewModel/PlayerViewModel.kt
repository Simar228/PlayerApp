package com.example.sound.Presentation.playerUi.viewModel

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import com.example.sound.Presentation.playerUi.PlayerConnectionState
import com.example.sound.Presentation.playerUi.PlayerUIEvent
import com.example.sound.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private var positionUpdatesJob: Job? = null
    private val playerController: PlayerController = PlayerController(
        controllerFuture = controllerFuture,
        controllerListenerExecutor = controllerListenerExecutor,
        onControllerReady = ::handleControllerReady
    )
    val uiState = playerController.mediaControllerState

    init {
        playerController.connect()
    }

    fun sendSong(
        queueSongs: List<Song> = emptyList(),
        song: Song,
        queueItemId: Long? = null
    ) {
        when (uiState.value.connectionState) {
            PlayerConnectionState.Connecting -> {
                playerController.showSelectedSong(song)
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
                playerController.play()
            }

            is PlayerUIEvent.Pause -> {
                playerController.pause()
            }

            is PlayerUIEvent.SeekTo -> {
                playerController.seekTo(event.positionMs)
            }

            is PlayerUIEvent.NextSong -> {
                playerController.next()
            }

            is PlayerUIEvent.PreviousSong -> {
                playerController.previous()
            }
        }
    }

    override fun onCleared() {
        stopPositionUpdates()
        playerController.release()
        super.onCleared()
    }

    fun startPositionUpdates() {
        if (positionUpdatesJob?.isActive == true) return
        positionUpdatesJob = viewModelScope.launch {
            while (isActive) {
                playerController.updatePosition()
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

    private fun handleControllerReady() {
        val request = pendingPlaybackRequest ?: return
        pendingPlaybackRequest = null
        playerController.showSelectedSong(request.selectedSong)
        playSong(
            queueSongs = request.queueSongs,
            selectedSong = request.selectedSong,
            queueItemId = request.queueItemId
        )
    }


}


private data class PendingPlaybackRequest(
    val queueSongs: List<Song>,
    val selectedSong: Song,
    val queueItemId: Long?
)


const val TAG = "PlayerViewModel"
