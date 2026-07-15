package com.example.sound.Presentation.playerUi

sealed interface PlayerUIEvent {
    data object Play: PlayerUIEvent
    data object Pause: PlayerUIEvent
    data object NextSong: PlayerUIEvent
    data object PreviousSong: PlayerUIEvent
    data class SeekTo(
        val positionMs: Long
    ) : PlayerUIEvent
}