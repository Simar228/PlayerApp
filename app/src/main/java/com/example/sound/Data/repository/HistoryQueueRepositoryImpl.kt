package com.example.sound.Data.repository

import com.example.sound.Data.local.historyQueue.HistoryQueueDao
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.HistoryQueueRepository
import javax.inject.Inject

class HistoryQueueRepositoryImpl @Inject constructor(
    private val historyQueueDao: HistoryQueueDao
): HistoryQueueRepository {
    override suspend fun addHistoryItem(song: Song) {
        TODO("Not yet implemented")
    }
}