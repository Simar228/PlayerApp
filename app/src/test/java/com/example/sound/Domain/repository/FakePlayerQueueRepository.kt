package com.example.sound.Domain.repository

import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePlayerQueueRepository : PlayerQueueRepository {

    private var queueOfIds = listOf<Long>()
    private val _queueItemsFlow = MutableStateFlow<List<QueueItem>>(emptyList())

    private var queueItems: List<QueueItem>
        get() = _queueItemsFlow.value
        set(value) {
            _queueItemsFlow.value = value
        }
    private var idCounter = 0L

    fun fakeSetQueueItems(queueItems: List<QueueItem>) {
        this.queueItems = queueItems
        idCounter = (queueItems.maxOfOrNull { it.id } ?: -1L) + 1
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
        return _queueItemsFlow.asStateFlow()
    }

    override suspend fun saveQueueOrder(queueItemsIds: List<Long>) {
        queueOfIds = queueItemsIds
    }

    override suspend fun insertSongAtTheStart(song: Song) {

        val currentQueue = queueItems

        val newQueueItem = QueueItem(
            position = 0,
            song = song,
            id = idCounter++,
        )

        val updatedQueue = currentQueue
            .toMutableList()
            .apply {
                add(0, newQueueItem)
            }
            .mapIndexed { index, queueItem ->
                queueItem.copy(position = index)
            }

        queueItems = updatedQueue
    }


    override suspend fun insertSongAtTheEnd(song: Song) {
        val newQueueItem = QueueItem(
            position = queueItems.size,
            song = song,
            id = idCounter++
        )
        queueItems = queueItems + newQueueItem
    }
}