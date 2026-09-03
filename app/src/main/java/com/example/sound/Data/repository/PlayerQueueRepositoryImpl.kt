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


    override suspend fun moveQueueItemToCurrent(queueItemId: Long) {
        database.withTransaction {
            val currentQueue = queueDao.getQueue().toMutableList()
            val movedQueueItem =
                currentQueue.find { it.id == queueItemId } ?: return@withTransaction
            currentQueue.remove(movedQueueItem)
            currentQueue[0] = movedQueueItem.copy(position = 0)

            queueDao.replaceQueue(currentQueue.repositionQueue())
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
            val currentQueueItem = existingQueue.firstOrNull()
                ?.takeIf { item -> item.song.id == currentSong.id }
                ?.copy(
                    song = currentSong,
                    position = 0,
                    fromUser = true,
                )
                ?: currentSong.toQueueItemEntity(position = 0, fromUser = true)
            val newQueue = buildList {
                add(currentQueueItem)
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
                item.position > 0 && item.id == queueItemId && item.fromUser
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
                    maxOf(1, queueItems.indexOfLast { it.fromUser } + 1)
                        .coerceAtMost(queueItems.size)

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

            val insertionIndex = minOf(1, currentQueue.size)
            val queueItemEntity = song.toQueueItemEntity(insertionIndex, true)
            val updatedQueue = currentQueue.toMutableList().apply {
                add(insertionIndex, queueItemEntity)
            }


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
            database.withTransaction {
                val currentQueue = queueDao.getQueue()
                val currentItem = currentQueue.firstOrNull()
                    ?: return@withTransaction
                val upcomingItems = currentQueue.drop(1)
                val upcomingItemsById = upcomingItems.associateBy { item -> item.id }
                val reorderedItems = queueItemsIds
                    .asSequence()
                    .filterNot { queueItemId -> queueItemId == currentItem.id }
                    .mapNotNull(upcomingItemsById::get)
                    .distinctBy { item -> item.id }
                    .toList()
                val reorderedIds = reorderedItems.mapTo(mutableSetOf()) { item -> item.id }
                val omittedItems = upcomingItems.filterNot { item -> item.id in reorderedIds }

                queueDao.replaceQueue(
                    (listOf(currentItem) + reorderedItems + omittedItems).repositionQueue()
                )
            }
        }
    }

    private fun List<QueueItemEntity>.repositionQueue() =
        this.mapIndexed { index, entity ->
            entity.copy(position = index)
        }

}
