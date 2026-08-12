package com.example.sound.Presentation.playerUi.viewModel

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.playerUi.PlayerConnectionState
import com.example.sound.Presentation.playerUi.PlayerUiState
import com.example.sound.service.playback.toSong
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executor

class PlayerController(
    private val controllerListenerExecutor: Executor,
    private val onControllerReady: () -> Unit,
    private val controllerFuture: ListenableFuture<MediaController>

    ) {
    private var isConnectionStarted = false
    private val _mediaControllerState = MutableStateFlow(PlayerUiState())
    val mediaControllerState = _mediaControllerState.asStateFlow()
    private var controller: MediaController? = null
    private val playerListener = object : Player.Listener {

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            _mediaControllerState.update { state ->
                state.copy(
                    currentPosition = newPosition.positionMs.coerceAtLeast(0L)
                )
            }
        }

        override fun onMediaItemTransition(
            mediaItem: MediaItem?, reason: Int
        ) {
            val transitionedSong = mediaItem?.toSong()
            _mediaControllerState.update { state ->
                state.copy(
                    currentSong = transitionedSong,
                    currentPosition = 0L,
                    duration = transitionedSong?.duration ?: C.TIME_UNSET,
                )
            }
        }


        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _mediaControllerState.update { state ->
                state.copy(
                    isPlaying = isPlaying
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _mediaControllerState.update { state ->
                    state.copy(
                        duration = controller?.duration ?: C.TIME_UNSET,
                    )
                }
            }
            if (playbackState == Player.STATE_ENDED) {
                _mediaControllerState.update { state ->
                    state.copy(
                        currentPosition = state.duration
                    )
                }
            }
        }
    }

    fun connect() {
        if (isConnectionStarted) return
        isConnectionStarted = true
        Log.d(TAG, "Connecting MediaController")
        controllerFuture.addListener(
            {
                try {
                    val mediaController = controllerFuture.get()
                    controller = mediaController

                    mediaController.addListener(playerListener)

                    synchronizeWithController(mediaController)
                    _mediaControllerState.update { state ->
                        state.copy(
                            connectionState = PlayerConnectionState.Ready
                        )
                    }

                    Log.d(TAG, "MediaController is ready")
                    onControllerReady()

                } catch (error: Exception) {
                    controller = null
                    Log.e(TAG, "Error connecting MediaController", error)
                    _mediaControllerState.update { state ->
                        state.copy(
                            connectionState = PlayerConnectionState.Error(error)
                        )
                    }
                }
            }, controllerListenerExecutor
        )
    }

    fun release() {
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
        controller = null
    }

    private fun synchronizeWithController(
        mediaController: MediaController
    ) {
        _mediaControllerState.update { state ->
            state.copy(
                currentPosition = mediaController.currentPosition.coerceAtLeast(0L),
                currentSong = mediaController.currentMediaItem?.toSong(),
                duration = mediaController.duration,
                isPlaying = mediaController.isPlaying
            )
        }
    }

    fun showSelectedSong(song: Song) {
        _mediaControllerState.update { state ->
            state.copy(
                currentSong = song,
                currentPosition = 0L,
                duration = song.duration
            )
        }
    }

    fun updatePosition() {
        val mediaController = controller ?: return
        if (!mediaController.isPlaying) return

        _mediaControllerState.update { state ->
            state.copy(
                currentPosition = mediaController.currentPosition.coerceAtLeast(0L)
            )
        }
    }

    fun play() {
        controller?.play()
    }

    fun pause() {
        controller?.pause()
    }

    fun seekTo(positionMs: Long) {
        _mediaControllerState.update { state ->
            state.copy(
                currentPosition = positionMs,
            )
        }
        controller?.seekTo(positionMs)
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
    }

    private companion object {
        const val TAG = "PlayerController"
    }

}

