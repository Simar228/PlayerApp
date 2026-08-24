package com.example.sound.Presentation.playerUi.viewModel

fun interface PlayerControllerProvider {
    fun create(onControllerReady: () -> Unit): PlayerController
}
