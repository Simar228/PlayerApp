package com.example.sound.Domain.repository

import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.QueueItem
import kotlinx.coroutines.flow.Flow


interface PlayerQueueRepository {

    fun observeQueue(): Flow<List<QueueItem>>

    suspend fun saveQueue(queue: List<QueueItem>)

    suspend fun getQueue(): List<QueueItem>

    suspend fun savePlayerState(
        currentQueueItemId: Long?,
        positionMs: Long
    )

    suspend fun getPlayerState(): PlayerState?
}