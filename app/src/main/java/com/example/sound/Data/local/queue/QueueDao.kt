package com.example.sound.Data.local.queue


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {

    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    fun observeQueue(): Flow<List<QueueItemEntity>>

    @Query("SELECT * FROM queue_items ORDER BY position ASC")
    suspend fun getQueue(): List<QueueItemEntity>

    @Insert
    suspend fun insertQueueItem(item: QueueItemEntity)

    @Insert
    suspend fun insertQueueItems(items: List<QueueItemEntity>)

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()

    @Delete
    suspend fun deleteQueueItem(item: QueueItemEntity)

    @Update
    suspend fun updateQueueItem(item: QueueItemEntity)

    @Transaction
    suspend fun replaceQueue(items: List<QueueItemEntity>) {
        clearQueue()
        insertQueueItems(items)
    }

}