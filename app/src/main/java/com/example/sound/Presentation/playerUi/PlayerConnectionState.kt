package com.example.sound.Presentation.playerUi


sealed interface PlayerConnectionState {

    data object Connecting : PlayerConnectionState

    data object Ready : PlayerConnectionState

    data class Error(
        val cause: Throwable
    ) : PlayerConnectionState
}