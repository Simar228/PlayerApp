package com.example.sound.Data.repository

import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Data.local.queue.QueueItemEntity
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
    private val database: AppDatabase,
) : PlayerQueueRepository {


    override suspend fun chooseSongFromQueue(queueItemId: Long) {
        val currentList = queueDao.getQueue().toMutableList()
        val removedItemIndex = currentList.find { it.id == queueItemId && it.fromUser }?.position
        removedItemIndex?.let { removedItemIndex ->
            currentList.removeAt(removedItemIndex)
            queueDao.replaceQueue(currentList)
        }

    }


    override suspend fun setCurrentSong(
        currentSong: Song, songs: List<Song>
    ) {
        database.withTransaction {
            val existingQueue = queueDao.getQueue()

            val explicitQueue = existingQueue
                .drop(1)
                .filter { it.fromUser }

            val currentIndex = songs.indexOfFirst { it.id == currentSong.id }
            val rotatedDefaultQueue = if (currentIndex >= 0) {
                songs.drop(currentIndex + 1) + songs.take(currentIndex)
            } else {
                songs
            }
            val newQueue = buildList {
                add(currentSong.toQueueItemEntity(position = 0, fromUser = true))
                addAll(explicitQueue)
                addAll(
                    rotatedDefaultQueue.map { song ->
                        song.toQueueItemEntity(position = 0, fromUser = false)
                    }
                )
            }

            queueDao.replaceQueue(newQueue.repositionQueue())

        }
    }


    override suspend fun deleteQueueItemById(
        queueItemId: Long
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val currentQueue = queueDao.getQueue()

            val updatedQueue = currentQueue.filterNot { item ->
                item.id == queueItemId && item.fromUser
            }

            if (updatedQueue.size == currentQueue.size) {
                return@withTransaction
            }
            queueDao.replaceQueue(updatedQueue.repositionQueue())
        }
    }


    override suspend fun clearQueue() = withContext(Dispatchers.IO) {
        queueDao.clearQueue()
    }

    override suspend fun insertSongAtTheEnd(song: Song) =
        withContext(Dispatchers.IO) {
            database.withTransaction {
                val queueItems = queueDao.getQueue().toMutableList()

                val insertionIndex =
                    queueItems.indexOfLast { it.fromUser } + 1

                queueItems.add(
                    insertionIndex,
                    song.toQueueItemEntity(
                        position = insertionIndex,
                        fromUser = true,
                    )
                )

                queueDao.replaceQueue(
                    queueItems.repositionQueue()
                )
            }
        }


    override suspend fun insertSongAtTheStart(song: Song) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val currentQueue = queueDao.getQueue()

            val queueItemEntity = song.toQueueItemEntity(
                1, true
            )
            val updatedQueue = currentQueue.toMutableList().apply { add(1, queueItemEntity) }


            queueDao.replaceQueue(updatedQueue.repositionQueue())
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

    private fun List<QueueItemEntity>.repositionQueue() =
        this.mapIndexed { index, entity ->
            entity.copy(position = index)
        }

}