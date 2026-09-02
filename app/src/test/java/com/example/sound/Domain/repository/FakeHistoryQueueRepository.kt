package com.example.sound.Domain.repository

import com.example.sound.Domain.model.HistoryItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeHistoryQueueRepository(
    initialHistory: List<HistoryItem> = emptyList(),
) : HistoryQueueRepository {

    private val history = MutableStateFlow(initialHistory)

    val addedHistoryItems = mutableListOf<HistoryItem>()

    override fun observeHistoryQueue(): Flow<List<HistoryItem>> {
        return history.asStateFlow()
    }

    override suspend fun addHistoryItem(
        song: Song,
        playedAt: Long,
    ) {
        val historyItem = HistoryItem(
            song = song,
            playedAt = playedAt,
            position = 0,
        )
        addedHistoryItems += historyItem
        history.value = listOf(historyItem) + history.value.map { item ->
            item.copy(position = item.position + 1)
        }
    }

    fun setHistoryQueue(historyItems: List<HistoryItem>) {
        history.value = historyItems
    }
}
