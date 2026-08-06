package com.example.sound.Domain.repository


import com.example.sound.Domain.model.PlayerState
import kotlinx.coroutines.flow.Flow

interface PlayerStateRepository {
    fun observePlayerState(): Flow<PlayerState?>
    suspend fun setPlayerState(
        playerState: PlayerState
    )
}
