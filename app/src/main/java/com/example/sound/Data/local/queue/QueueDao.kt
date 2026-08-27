package com.example.sound.Data.local.queue


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.sound.Data.local.DatabaseTableNames
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {

    @Transaction
    suspend fun appendQueueItem(item: QueueItemEntity) {
        val currentQueue = getQueue()
        val nextPosition =
            (currentQueue.maxOfOrNull { queueItem -> queueItem.position } ?: -1) + 1

        insertQueueItem(
            item.copy(position = nextPosition)
        )
    }
    @Query("SELECT * FROM ${DatabaseTableNames.QUEUE_ITEMS} ORDER BY position ASC")
    fun observeQueue(): Flow<List<QueueItemEntity>>

    @Query("SELECT * FROM ${DatabaseTableNames.QUEUE_ITEMS} ORDER BY position ASC")
    suspend fun getQueue(): List<QueueItemEntity>

    @Insert
    suspend fun insertQueueItem(item: QueueItemEntity)

    @Insert
    suspend fun insertQueueItems(items: List<QueueItemEntity>)

    @Query("DELETE FROM ${DatabaseTableNames.QUEUE_ITEMS}")
    suspend fun clearQueue()


    @Transaction
    suspend fun reorderQueue(queueItemIds: List<Long>) {
        val currentQueue = getQueue()
        val itemsById = currentQueue.associateBy { item ->
            item.id
        }
        val reorderedItems = queueItemIds
            .mapNotNull { queueItemId ->
                itemsById[queueItemId]
            }
            .distinctBy { item -> item.id }
        val reorderedIds = reorderedItems.map { item -> item.id }

        val remainingItems = currentQueue.filterNot { item ->
            item.id in reorderedIds
        }

        val finalQueue = (reorderedItems + remainingItems)
            .mapIndexed { index, item ->
                item.copy(position = index)
            }

        replaceQueue(finalQueue)
    }

    @Transaction
    suspend fun replaceQueue(queueItems: List<QueueItemEntity>) {
        clearQueue()
        insertQueueItems(queueItems)
    }

}
