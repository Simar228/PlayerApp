package com.example.sound.Domain.repository

import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow

class FakePlayerQueueRepository : PlayerQueueRepository {

    private var queueOfIds = listOf<Long>()
    private var queueItems = listOf<QueueItem>()

    fun fakeSetQueueItems(queueItems: List<QueueItem>) {
        this.queueItems = queueItems
    }

    fun getFakeQueueItems(): List<QueueItem> {
        return queueItems
    }

    fun getQueueOfIds(): List<Long> {
        return queueOfIds
    }

    override suspend fun deleteQueueItemById(queueItemId: Long) {
        val currentQueue = queueItems

        val updatedQueue = currentQueue.filterNot { item ->
            item.id == queueItemId
        }

        if (updatedQueue.size == currentQueue.size) {
            return
        }

        val reindexedQueue = updatedQueue.mapIndexed { index, item ->
            item.copy(position = index)
        }

        queueItems = reindexedQueue
    }

    override suspend fun clearQueue() {
        queueItems = emptyList()
    }

    override fun observeQueue(): Flow<List<QueueItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveQueueOrder(queueItemsIds: List<Long>) {
        queueOfIds = queueItemsIds.toMutableList()
    }

    override suspend fun insertQueueItem(queueItem: QueueItem) {
        val currentQueue = queueItems
        val safePosition = queueItem.position.coerceIn(0, currentQueue.size)

        val updatedQueue = currentQueue
            .toMutableList()
            .apply {
                add(safePosition, queueItem)
            }
            .mapIndexed { index, queueItem ->
                queueItem.copy(position = index)
            }

        queueItems = updatedQueue
    }

    override suspend fun insertQueueItemAtTheEnd(song: Song) {
        val length = queueItems.size
        val mutableQueueItems = queueItems.toMutableList()
        mutableQueueItems.add(
            QueueItem(
                position = length,
                song = song,
                id = length.toLong()
            )
        )
        queueItems = mutableQueueItems.toList()
    }
}