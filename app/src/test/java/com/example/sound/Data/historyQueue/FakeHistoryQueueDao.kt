package com.example.sound.Data.historyQueue

import com.example.sound.Data.local.historyQueue.HistoryQueueDao
import com.example.sound.Data.local.historyQueue.HistoryQueueItemEntity
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHistoryQueueDao : HistoryQueueDao {

    private val _historyQueueItemEntity =
        MutableStateFlow<List<HistoryQueueItemEntity>>(emptyList())
    val historyQueueItemEntity: List<Song>
        get() = _historyQueueItemEntity.value.map { it.song }

    override fun observeHistoryQueueItems(): Flow<List<HistoryQueueItemEntity>> {
        return _historyQueueItemEntity
    }


    override suspend fun clearHistoryQueueItems() {
        _historyQueueItemEntity.value = emptyList()
    }

    override suspend fun insertHistoryQueueItems(historyQueueItemEntities: List<HistoryQueueItemEntity>) {
        _historyQueueItemEntity.value += historyQueueItemEntities
    }

    fun setHistoryQueueItemEntity(historyQueueItemEntity: List<HistoryQueueItemEntity>) {
        _historyQueueItemEntity.value = historyQueueItemEntity
    }

}