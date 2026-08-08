package com.example.sound.Data.local.defualtQueue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface DefaultQueueDao{
    @Query("SELECT * FROM defaultQueue_items ORDER BY position ASC")
    suspend fun getDefaultQueue(): List<DefaultQueueItemEntity>
    @Insert
    suspend fun insertDefaultQueueItems(items: List<DefaultQueueItemEntity>)

    @Query("DELETE FROM defaultQueue_items")
    suspend fun clearDefaultQueue()

    @Transaction
    suspend fun replaceDefaultQueue(items: List<DefaultQueueItemEntity>) {
        clearDefaultQueue()
        insertDefaultQueueItems(items)
    }
}