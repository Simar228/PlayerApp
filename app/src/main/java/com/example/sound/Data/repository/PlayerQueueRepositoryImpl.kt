package com.example.sound.Data.repository

import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Data.local.queue.toDomain
import com.example.sound.Data.local.queue.toEntity
import com.example.sound.Data.local.queue.toQueueItemEntity
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerQueueRepositoryImpl @Inject constructor(
    private val queueDao: QueueDao,
) : PlayerQueueRepository {


    override suspend fun deleteSongByPosition(
        song: Song,
        position: Int
    ) {
        val songList = queueDao.getQueue()
        val queueItem = songList.getOrNull(position) ?: return
        if (queueItem.songId != song.id) {
            return
        }
        val updatedList = songList.toMutableList()
        updatedList.removeAt(position)

        val reindexedList = updatedList.mapIndexed { index, item ->
            item.copy(position = index)
        }
        queueDao.replaceQueue(reindexedList)
    }

    override suspend fun deleteFirstSong(currentSong: Song) {
        val songQueue = queueDao.getQueue()
        val firstSong = songQueue.firstOrNull()?.songId
        val listWithoutFirstSong = songQueue.toMutableList().drop(1)
        if (firstSong == currentSong.id) {
            queueDao.replaceQueue(listWithoutFirstSong)
        }

    }


    override suspend fun setCurrentSong(song: Song) {
        val currentQueueList = queueDao.getQueue().mapIndexed { index, item ->
            if (index == 0) song.toQueueItemEntity(0) else item
        }
        queueDao.replaceQueue(currentQueueList)
    }


    override suspend fun clearQueue() {
        queueDao.clearQueue()
    }

    override suspend fun insertSong(song: Song) {
        val queueItems = queueDao.getQueue()
        val nextPosition: Int = (queueItems.maxOfOrNull { it.position } ?: -1) + 1
        val currentQueueItem = song.toQueueItemEntity(nextPosition)
        queueDao.insertQueueItem(currentQueueItem)
    }

    override suspend fun insertSongByPosition(song: Song, position: Int) {
        val queueItemEntity = song.toQueueItemEntity(position)
        val songList = queueDao.getQueue()
        val safePosition = position.coerceIn(0, songList.size)
        val updatedList = songList
            .toMutableList()
            .apply {
                add(safePosition, queueItemEntity)
            }
        val reindexedList = updatedList.mapIndexed { index, item ->
            item.copy(position = index)
        }
        queueDao.replaceQueue(reindexedList)
    }

    override fun observeQueue(): Flow<List<QueueItem>> {
        return queueDao.observeQueue()
            .map { items ->
                items.map { it.toDomain() }
            }
    }

    override suspend fun getQueue(): List<QueueItem> {
        return queueDao.getQueue().map { it.toDomain() }
    }


    override suspend fun saveQueue(queue: List<QueueItem>) {
        val entities = queue.mapIndexed { index, item ->
            item.copy(position = index).toEntity()
        }
        queueDao.replaceQueue(entities)
    }

}