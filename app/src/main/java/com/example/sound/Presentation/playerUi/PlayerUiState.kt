package com.example.sound.Presentation.playerUi

import com.example.sound.Domain.model.Song

data class PlayerUiState(
    val connectionState: PlayerConnectionState =
        PlayerConnectionState.Connecting,
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
)