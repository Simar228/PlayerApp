package com.example.sound.Domain.repository

import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow

class FakePlayerQueueRepository : PlayerQueueRepository {

    private var queueOfIds = listOf<Long>()
    private var songsQueue = listOf<QueueItem>()

    fun fakeSetQueueItems(queueItems: List<QueueItem>) {
        songsQueue = queueItems
    }

    fun getFakeQueueItems(): List<QueueItem> {
        return songsQueue
    }

    fun getQueueOfIds(): List<Long> {
        return queueOfIds
    }

    override suspend fun deleteQueueItemById(queueItemId: Long) {
        val currentQueue = songsQueue

        val updatedQueue = currentQueue.filterNot { item ->
            item.id == queueItemId
        }

        if (updatedQueue.size == currentQueue.size) {
            return
        }

        val reindexedQueue = updatedQueue.mapIndexed { index, item ->
            item.copy(position = index)
        }

        songsQueue = reindexedQueue
    }

    override suspend fun clearQueue() {
        TODO("Not yet implemented")
    }

    override suspend fun insertSong(song: Song) {
        TODO("Not yet implemented")
    }

    override suspend fun insertSongByPosition(
        song: Song,
        position: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun observeQueue(): Flow<List<QueueItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveQueueOrder(queueItemsIds: List<Long>) {
        queueOfIds = queueItemsIds
    }

}