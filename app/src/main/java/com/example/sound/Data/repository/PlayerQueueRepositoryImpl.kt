package com.example.sound.Data.repository

import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Data.local.queue.toDomain
import com.example.sound.Data.local.queue.toEntity
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerQueueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlayerQueueRepositoryImpl @Inject constructor(
    private val queueDao: QueueDao,
    private val database: AppDatabase,
) : PlayerQueueRepository {


    override suspend fun deleteQueueItemById(
        queueItemId: Long
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val currentQueue = queueDao.getQueue()

            val updatedQueue = currentQueue.filterNot { item ->
                item.id == queueItemId
            }

            if (updatedQueue.size == currentQueue.size) {
                return@withTransaction
            }

            val reindexedQueue = updatedQueue.mapIndexed { index, item ->
                item.copy(position = index)
            }

            queueDao.replaceQueue(reindexedQueue)
        }
    }


    override suspend fun clearQueue() = withContext(Dispatchers.IO) {
        queueDao.clearQueue()
    }

    override suspend fun insertSongAtTheEnd(song: Song) =
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val queueItems = queueDao.getQueue()

                val lastPosition = queueItems.maxOfOrNull { it.position }

                val insertedQueueItemEntity =
                    QueueItem(id = 0, position = lastPosition?.plus(1) ?: 0, song = song).toEntity()
                queueDao.insertQueueItem(insertedQueueItemEntity)

            }
        }


    override suspend fun insertSongAtTheStart(song: Song) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val currentQueue = queueDao.getQueue()

            val queueItem = QueueItem(
                id = 0,
                position = 0,
                song = song
            )

            val updatedQueue = currentQueue
                .toMutableList()
                .apply { add(0, queueItem.toEntity()) }
                .mapIndexed { index, entity ->
                    entity.copy(position = index)
                }

            queueDao.replaceQueue(updatedQueue)
        }
    }

    override fun observeQueue(): Flow<List<QueueItem>> {
        return queueDao.observeQueue().map { items ->
            items.map { it.toDomain() }
        }
    }

    override suspend fun saveQueueOrder(queueItemsIds: List<Long>) {
        withContext(Dispatchers.IO) {
            queueDao.reorderQueue(queueItemsIds)
        }
    }

}