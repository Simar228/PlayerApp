package com.example.sound.Data.local.historyQueue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.sound.Data.local.DatabaseTableNames
import kotlinx.coroutines.flow.Flow


@Dao
interface HistoryQueueDao {

    @Query("SELECT * FROM ${DatabaseTableNames.HISTORY_QUEUE_ITEMS} ORDER BY position ASC")
    fun observeHistoryQueueItems(): Flow<List<HistoryQueueItemEntity>>

    @Transaction
    suspend fun setHistoryQueueItems(historyQueueItemEntities: List<HistoryQueueItemEntity>) {
        clearHistoryQueueItems()
        insertHistoryQueueItems(historyQueueItemEntities)
    }

    @Query("DELETE FROM ${DatabaseTableNames.HISTORY_QUEUE_ITEMS}")
    suspend fun clearHistoryQueueItems()

    @Insert
    suspend fun insertHistoryQueueItems(historyQueueItemEntities: List<HistoryQueueItemEntity>)

}