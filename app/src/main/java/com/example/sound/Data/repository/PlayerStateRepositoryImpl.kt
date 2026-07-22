package com.example.sound.Data.repository

import android.util.Log
import com.example.sound.Data.local.playerstate.PlayerStateDao
import com.example.sound.Data.local.playerstate.toDomain
import com.example.sound.Data.local.playerstate.toEntity
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.repository.PlayerStateRepository
import javax.inject.Inject

class PlayerStateRepositoryImpl @Inject constructor(
    private val playerStateDao: PlayerStateDao
) : PlayerStateRepository {
    override suspend fun getPlayerState(): PlayerState? {
        return playerStateDao.getPlayerState()?.toDomain()
    }

    override suspend fun setPlayerState(playerState: PlayerState) {
        Log.d("!!!", playerState.toString())
        playerStateDao.savePlayerState(playerState.toEntity())
    }

}