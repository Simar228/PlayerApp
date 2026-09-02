package com.example.sound.Data.repository

import com.example.sound.Data.local.historyQueue.HistoryQueueDao
import com.example.sound.Data.local.historyQueue.toHistoryQueueItemEntity
import com.example.sound.Domain.model.HistoryItem
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.HistoryQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryQueueRepositoryImpl @Inject constructor(
    private val historyQueueDao: HistoryQueueDao,
) : HistoryQueueRepository {
    override fun observeHistoryQueue(): Flow<List<HistoryItem>> {
        return historyQueueDao.observeHistoryQueueItems().map { itemEntities ->
            itemEntities.map { item ->
                HistoryItem(item.song, item.playedAt, item.position)
            }
        }
    }


    override suspend fun addHistoryItem(
        song: Song,
        playedAt: Long
    ) {
        val currentHistory = historyQueueDao
            .observeHistoryQueueItems()
            .first()

        if (song == currentHistory.firstOrNull()?.song) {
            return
        }

        val newItem = song.toHistoryQueueItemEntity(
            position = 0,
            playedAt = playedAt
        )

        val updatedHistory =
            (listOf(newItem) + currentHistory)
                .take(100)
                .mapIndexed { index, item ->
                    item.copy(position = index)
                }

        historyQueueDao.setHistoryQueueItems(updatedHistory)
    }
}