package com.example.sound.Data.repository

import com.example.sound.Data.local.playerstate.PlayerStateDao
import com.example.sound.Data.local.playerstate.toDomain
import com.example.sound.Data.local.playerstate.toPlayerStateEntity
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerStateRepositoryImpl @Inject constructor(
    private val playerStateDao: PlayerStateDao
) : PlayerStateRepository {
    override fun observePlayerState(): Flow<PlayerState?> {
        return playerStateDao.observePlayerState().map { playerStateEntity ->
            playerStateEntity?.toDomain()

        }
    }

    override suspend fun setPlayerState(playerState: PlayerState) {
        val song = playerState.currentSong ?: return
        playerStateDao.savePlayerState(song.toPlayerStateEntity())
    }

}
