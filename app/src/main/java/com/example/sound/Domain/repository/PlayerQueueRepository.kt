package com.example.sound.Domain.repository

import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow


interface PlayerQueueRepository {

    suspend fun clearQueue()
    suspend fun insertQueueItem(item: QueueItem)

    suspend fun insertSongByIndex(song: Song, index: Int)
    fun observeQueue(): Flow<List<QueueItem>>

    suspend fun saveQueue(queue: List<QueueItem>)

    suspend fun getQueue(): List<QueueItem>

    suspend fun savePlayerState(
        currentQueueItemId: Long?,
        positionMs: Long
    )

    suspend fun getPlayerState(): PlayerState?
}