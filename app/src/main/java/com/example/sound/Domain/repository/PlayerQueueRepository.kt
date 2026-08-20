package com.example.sound.Domain.repository


import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow


interface PlayerQueueRepository {

    suspend fun deleteQueueItemById(queueItemId: Long)
    
    suspend fun clearQueue()

    fun observeQueue(): Flow<List<QueueItem>>

    suspend fun saveQueueOrder(queueItemsIds: List<Long>)

    suspend fun insertQueueItem(queueItem: QueueItem)
    suspend fun insertQueueItemAtTheEnd(song: Song)
}