package com.example.sound.Data.repository

import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Data.local.queue.toDomain
import com.example.sound.Data.local.queue.toQueueItemEntity
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
) : PlayerQueueRepository {


    override suspend fun deleteQueueItem(
        queueItemId: Long
    ) {
        queueDao.deleteQueueItemAndReindex(queueItemId)
    }


    override suspend fun clearQueue() {
        queueDao.clearQueue()
    }

    override suspend fun insertSong(song: Song) {
        queueDao.appendQueueItem(
            song.toQueueItemEntity(position = 0)
        )
    }

    override suspend fun insertSongByPosition(song: Song, position: Int) {
        queueDao.insertQueueItemAtPosition(
            item = song.toQueueItemEntity(position)
        )
    }

    override fun observeQueue(): Flow<List<QueueItem>> {
        return queueDao.observeQueue()
            .map { items ->
                items.map { it.toDomain() }
            }
    }

    override suspend fun saveQueueOrder(queueItemsIds: List<Long>) {
        withContext(Dispatchers.IO) {
            queueDao.reorderQueue(queueItemsIds)
        }
    }

}