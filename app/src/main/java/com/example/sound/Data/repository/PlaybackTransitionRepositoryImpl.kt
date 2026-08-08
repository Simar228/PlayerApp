package com.example.sound.Data.repository


import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.playerstate.PlayerStateDao
import com.example.sound.Data.local.playerstate.toPlayerStateEntity
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import javax.inject.Inject

class PlaybackTransitionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val playerStateDao: PlayerStateDao,
    private val queueDao: QueueDao,
) : PlaybackTransitionRepository {

    override suspend fun saveTransition(song: Song, queueItemId: Long?) {
        database.withTransaction {
            playerStateDao.savePlayerState(
                song.toPlayerStateEntity()
            )

            if (queueItemId != null) {
                queueDao.deleteQueueItemAndReindex(queueItemId)
            }
        }
    }
}