package com.example.sound.Data.repository

import android.util.Log
import androidx.core.net.toUri
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
    override fun observePlayerState(): Flow<Song> {
        return playerStateDao.observePlayerState().map { playerStateEntity ->
            Song(
                id = playerStateEntity.currentSongId,
                title = playerStateEntity.currentSongTitle,
                artist = playerStateEntity.currentSongArtist,
                duration = playerStateEntity.currentSongDuration,
                uri = playerStateEntity.currentSongUri.toUri(),
                album = playerStateEntity.currentSongAlbum,
                genre = playerStateEntity.currentSongGenre,
                art = playerStateEntity.currentSongArtUri?.toUri()
            )
        }
    }

    override suspend fun getPlayerState(): PlayerState? {
        return playerStateDao.getPlayerState()?.toDomain()
    }

    override suspend fun setPlayerState(song: Song) {
        val playerStateEntity = song.toPlayerStateEntity()
        playerStateDao.savePlayerState(playerStateEntity)
    }

}