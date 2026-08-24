package com.example.sound.Presentation.playerUi.viewModel

import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.playerUi.PlayerConnectionState
import com.example.sound.Presentation.playerUi.PlayerUiState
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.Futures
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakePlayerController : PlayerController(
    controllerListenerExecutor = { it.run() },
    onControllerReady = {},
    controllerFuture = Futures.immediateFailedFuture<MediaController>(
        IllegalStateException("FakePlayerController does not use a MediaController")
    )
) {
    private val _mediaControllerState = MutableStateFlow(PlayerUiState())
    override val mediaControllerState = _mediaControllerState.asStateFlow()

    var connectCalled = false
        private set
    var releaseCalled = false
        private set
    var showSelectedSongCalledWith: Song? = null
        private set

    var onControllerReady: (() -> Unit)? = null

    val invokedEvents = mutableListOf<String>()

    override fun connect() {
        connectCalled = true
    }

    override fun release() {
        releaseCalled = true
    }

    override fun showSelectedSong(song: Song) {
        showSelectedSongCalledWith = song
        _mediaControllerState.update { it.copy(currentSong = song) }
    }

    override fun updatePosition() {
        invokedEvents.add("updatePosition")
    }

    override fun play() { invokedEvents.add("play") }
    override fun pause() { invokedEvents.add("pause") }
    override fun next() { invokedEvents.add("next") }
    override fun previous() { invokedEvents.add("previous") }

    override fun seekTo(positionMs: Long) {
        invokedEvents.add("seekTo-$positionMs")
        _mediaControllerState.update { it.copy(currentPosition = positionMs) }
    }

    fun emitState(state: PlayerUiState) {
        _mediaControllerState.value = state
    }

    fun triggerReady() {
        _mediaControllerState.update { it.copy(connectionState = PlayerConnectionState.Ready) }
        onControllerReady?.invoke()
    }
}
