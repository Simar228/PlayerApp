package com.example.sound.Data.local.historyQueue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.sound.Data.local.DatabaseTableNames


@Dao
interface HistoryQueueDao {

    @Query("SELECT * FROM ${DatabaseTableNames.HISTORY_QUEUE_ITEMS} ORDER BY position ASC")
    suspend fun getHistoryQueueItems(): List<HistoryQueueItemEntity?>

    @Insert
    suspend fun addHistoryQueueItems(historyQueueItemEntities: List<HistoryQueueItemEntity>)

}