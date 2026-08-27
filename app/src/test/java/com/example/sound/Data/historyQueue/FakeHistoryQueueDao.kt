package com.example.sound.Data.historyQueue

import com.example.sound.Data.local.defualtQueue.toDefaultQueueEntity
import com.example.sound.Data.local.historyQueue.HistoryQueueDao
import com.example.sound.Data.local.historyQueue.HistoryQueueItemEntity
import com.example.sound.Data.local.queue.toQueueItemEntity
import com.example.sound.Domain.model.Song

class FakeHistoryQueueDao : HistoryQueueDao {

    var historyQueueSong: List<Song> = emptyList()
        private set


    fun setHistoryQueueSong(songs: List<Song>){
        historyQueueSong = songs
    }

    override suspend fun getHistoryQueueItems(): List<HistoryQueueItemEntity?> {
        TODO("Not yet implemented")
    }

    override suspend fun addHistoryQueueItems(historyQueueItemEntities: List<HistoryQueueItemEntity>) {
        TODO("Not yet implemented")
    }

}