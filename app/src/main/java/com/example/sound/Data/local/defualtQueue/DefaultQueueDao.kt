package com.example.sound.Data.local.defualtQueue

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.sound.Data.local.queue.QueueItemEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface DefaultQueueDao{
    @Insert
    suspend fun insertDefaultQueueItems(items: List<DefaultQueueItemEntity>)

    @Query("DELETE FROM defaultQueue_items")
    suspend fun clearDefaultQueue()

    @Query("SELECT * FROM defaultQueue_items ORDER BY position ASC")
    fun observeDefaultQueue(): Flow<List<DefaultQueueItemEntity>>

    @Query("SELECT * FROM defaultQueue_items ORDER BY position ASC")
    suspend fun getDefaultQueue(): List<DefaultQueueItemEntity>

    @Transaction
    suspend fun replaceDefaultQueue(items: List<DefaultQueueItemEntity>) {
        clearDefaultQueue()
        insertDefaultQueueItems(items)
    }
}