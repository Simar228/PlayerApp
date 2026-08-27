package com.example.sound.Domain.repository

import com.example.sound.Domain.model.HistoryItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow

interface HistoryQueueRepository {

    fun observeHistoryQueue(): Flow<List<HistoryItem>>

    suspend fun addHistoryItem(
        song: Song,
        playedAt: Long
    )

}