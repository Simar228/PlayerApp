package com.example.sound.Domain.repository


import com.example.sound.Domain.model.PlayerState

interface PlayerStateRepository {
    suspend fun setPlayerState(
        playerState: PlayerState
    )
}
