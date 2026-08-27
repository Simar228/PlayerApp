package com.example.sound.Data.repository

import com.example.sound.Data.local.historyQueue.HistoryQueueDao
import com.example.sound.Data.local.historyQueue.toHistoryQueueItemEntity
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.HistoryQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryQueueRepositoryImpl @Inject constructor(
    private val historyQueueDao: HistoryQueueDao,
) : HistoryQueueRepository {
    override fun observeHistoryQueue(): Flow<List<Song>> {
        return historyQueueDao.observeHistoryQueueItems().map { songs ->
            songs.map { it.song }
        }
    }


    override suspend fun addHistoryItem(song: Song) {

        val historySongs = historyQueueDao
            .observeHistoryQueueItems().first()
            .map { it.song }


        if (song == historySongs.firstOrNull()) {
            return
        }

        val currentSongs = (listOf(song) + historySongs).take(100)
        val newHistorySong = currentSongs.mapIndexed { index, song ->
            song.toHistoryQueueItemEntity(index)
        }

        historyQueueDao.setHistoryQueueItems(newHistorySong)


    }
}