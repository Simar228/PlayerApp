package com.example.sound.Domain.repository

import com.example.sound.Domain.model.DefaultQueueItem
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow

interface DefaultQueueRepository {
    suspend fun observeQueue(): Flow<List<DefaultQueueItem>>
    suspend fun getDefaultQueue(): List<DefaultQueueItem>
    suspend fun updateDefaultQueue(newDefaultQueue: List<Song>)
}