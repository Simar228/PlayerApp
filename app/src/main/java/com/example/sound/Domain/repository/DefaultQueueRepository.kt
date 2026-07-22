package com.example.sound.Domain.repository

import com.example.sound.Domain.model.DefaultQueueItem
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow

interface DefaultQueueRepository {
    fun observeQueue(): Flow<List<Song>>
    suspend fun getDefaultQueue(): List<Song>
    suspend fun updateDefaultQueue(newDefaultQueue: List<Song>)
}