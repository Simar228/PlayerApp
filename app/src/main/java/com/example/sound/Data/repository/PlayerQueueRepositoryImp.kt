package com.example.sound.Data.repository

import com.example.sound.Data.local.playerstate.PlayerStateDao
import com.example.sound.Data.local.playerstate.PlayerStateEntity
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Data.local.queue.QueueItemEntity
import com.example.sound.Data.local.queue.toDomain
import com.example.sound.Data.local.queue.toEntity
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.model.toQueueItem
import com.example.sound.Domain.repository.PlayerQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerQueueRepositoryImpl @Inject constructor(
    private val queueDao: QueueDao,
    private val playerStateDao: PlayerStateDao
) : PlayerQueueRepository {


    override suspend fun clearQueue() {
        queueDao.clearQueue()
    }

    override suspend fun insertQueueItem(item: QueueItem) {
        queueDao.insertQueueItem(item.toEntity())
    }

    override suspend fun insertSongByIndex(song: Song, position: Int) {
        val queueItemEntity = song.toQueueItem(position)
        val songList = queueDao.getQueue()
            .toMutableList()
            .apply {
                add(position, queueItemEntity.toEntity())
            }
        val reindexedList = songList.mapIndexed { index, item ->
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
            item.toEntity().copy(position = index)
        }
        queueDao.replaceQueue(entities)
    }

    override suspend fun savePlayerState(
        currentQueueItemId: Long?,
        positionMs: Long
    ) {
        playerStateDao.savePlayerState(
            PlayerStateEntity(
                currentQueueItemId = currentQueueItemId,
                positionMs = positionMs
            )
        )
    }

    override suspend fun getPlayerState(): PlayerState? {
        return playerStateDao.getPlayerState()?.let {
            PlayerState(
                currentQueueItemId = it.currentQueueItemId,
                positionMs = it.positionMs
            )
        }
    }
}