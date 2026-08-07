package com.example.sound.Domain.repository


import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow


interface PlayerQueueRepository {

    suspend fun deleteQueueItem(queueItemId: Long)
    
    suspend fun clearQueue()
    suspend fun insertSong(song: Song)

    suspend fun insertSongByPosition(song: Song, position: Int)
    fun observeQueue(): Flow<List<QueueItem>>

    suspend fun saveQueue(queue: List<QueueItem>)


}