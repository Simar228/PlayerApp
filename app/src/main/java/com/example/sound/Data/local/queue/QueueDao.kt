package com.example.sound.Data.local.queue


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {

    @Transaction
    suspend fun deleteQueueItemAndReindex(queueItemId: Long) {
        val currentQueue = getQueue()

        val updatedQueue = currentQueue.filterNot { item ->
            item.id == queueItemId
        }

        if (updatedQueue.size == currentQueue.size) {
            return
        }

        val reindexedQueue = updatedQueue.mapIndexed { index, item ->
            item.copy(position = index)
        }

        replaceQueue(reindexedQueue)
    }

    @Transaction
    suspend fun appendQueueItem(item: QueueItemEntity) {
        val currentQueue = getQueue()
        val nextPosition =
            (currentQueue.maxOfOrNull { queueItem -> queueItem.position } ?: -1) + 1

        insertQueueItem(
            item.copy(position = nextPosition)
        )
    }

    @Transaction
    suspend fun insertQueueItemAtPosition(
        item: QueueItemEntity,
    ) {
        val currentQueue = getQueue()
        val safePosition = item.position.coerceIn(0, currentQueue.size)

        val updatedQueue = currentQueue
            .toMutableList()
            .apply {
                add(safePosition, item)
            }
            .mapIndexed { index, queueItem ->
                queueItem.copy(position = index)
            }

        replaceQueue(updatedQueue)
    }


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

    @Transaction
    suspend fun replaceQueue(items: List<QueueItemEntity>) {
        clearQueue()
        insertQueueItems(items)
    }

}