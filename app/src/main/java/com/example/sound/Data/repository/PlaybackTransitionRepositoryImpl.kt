package com.example.sound.Data.repository


import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.defualtQueue.toDefaultQueueEntity
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
    private val defaultQueueDao: DefaultQueueDao
) : PlaybackTransitionRepository {

    override suspend fun startPlayback(
        song: Song,
        defaultQueueSongs: List<Song>?,
        queueItemId: Long?
    ) {
        database.withTransaction {
            defaultQueueSongs?.let { songs ->
                val defaultQueueEntities =
                    songs.mapIndexed { index, queueSong ->
                        queueSong.toDefaultQueueEntity(index)
                    }

                defaultQueueDao.replaceDefaultQueue(defaultQueueEntities)
            }


            playerStateDao.savePlayerState(
                song.toPlayerStateEntity()
            )

            if (queueItemId != null) {
                queueDao.deleteQueueItemAndReindex(queueItemId)
            }
        }
    }

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
