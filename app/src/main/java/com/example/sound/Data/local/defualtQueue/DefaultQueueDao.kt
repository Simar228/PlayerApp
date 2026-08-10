package com.example.sound.Data.local.defualtQueue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.sound.Data.local.DatabaseTableNames


@Dao
interface DefaultQueueDao{
    @Query("SELECT * FROM ${DatabaseTableNames.DEFAULT_QUEUE_ITEMS} ORDER BY position ASC")
    suspend fun getDefaultQueue(): List<DefaultQueueItemEntity>
    @Insert
    suspend fun insertDefaultQueueItems(items: List<DefaultQueueItemEntity>)

    @Query("DELETE FROM ${DatabaseTableNames.DEFAULT_QUEUE_ITEMS}")
    suspend fun clearDefaultQueue()

    @Transaction
    suspend fun replaceDefaultQueue(items: List<DefaultQueueItemEntity>) {
        clearDefaultQueue()
        insertDefaultQueueItems(items)
    }
}
