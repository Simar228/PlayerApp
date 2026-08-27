package com.example.sound.Data.historyQueue

import com.example.sound.Data.local.historyQueue.HistoryQueueDao
import com.example.sound.Data.local.historyQueue.HistoryQueueItemEntity
import com.example.sound.Data.local.historyQueue.toHistoryQueueItemEntity
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeHistoryQueueDao : HistoryQueueDao {

    private val _historyQueueSong = MutableStateFlow<List<Song>>(emptyList())
    val historyQueueSong = _historyQueueSong.asStateFlow()


    fun setHistoryQueueSong(songs: List<Song>) {
        _historyQueueSong.value = songs
    }

    override fun observeHistoryQueueItems(): Flow<List<HistoryQueueItemEntity>> {
        return _historyQueueSong.map { songs ->
            songs.mapIndexed { index, song ->
                song.toHistoryQueueItemEntity(
                    position = index
                )
            }
        }
    }


    override suspend fun clearHistoryQueueItems() {
        _historyQueueSong.value = emptyList()
    }

    override suspend fun insertHistoryQueueItems(historyQueueItemEntities: List<HistoryQueueItemEntity>) {
        _historyQueueSong.value += historyQueueItemEntities.map { it.song }
    }


}