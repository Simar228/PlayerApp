package com.example.sound.Domain.repository

import com.example.sound.Data.local.playerstate.PlayerStateEntity
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlayerStateRepository {
    fun observePlayerState(): Flow<Song>
    suspend fun getPlayerState(): PlayerState?
    suspend fun setPlayerState(
        playerState: PlayerState
    )
}