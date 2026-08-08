package com.example.sound.Data.local.queue

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sound.Data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueueDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var queueDao: QueueDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        queueDao = database.queueDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun replaceQueue_removesOldItemsAndSavesNewItems() = runBlocking {
        // Given
        val oldItems = listOf(
            createQueueItem(id = 1, position = 0),
            createQueueItem(id = 2, position = 1)
        )

        queueDao.insertQueueItems(oldItems)

        val newItems = listOf(
            createQueueItem(id = 3, position = 0),
            createQueueItem(id = 4, position = 1)
        )

        // When
        queueDao.replaceQueue(newItems)

        // Then
        val actualQueue = queueDao.getQueue()

        val actualIds = actualQueue.map { item ->
            item.id
        }

        val expectedIds = listOf(3L, 4L)

        assertEquals(expectedIds, actualIds)
    }

    @Test
    fun getQueue_returnsItemsOrderedByPosition() = runBlocking {
        // Given
        val items = listOf(
            createQueueItem(id = 1, position = 2),
            createQueueItem(id = 2, position = 0),
            createQueueItem(id = 3, position = 1)
        )

        queueDao.insertQueueItems(items)

        // When
        val actualQueue = queueDao.getQueue()

        // Then
        val actualPositions = actualQueue.map { item ->
            item.position
        }

        val expectedPositions = listOf(0, 1, 2)
        assertEquals(expectedPositions, actualPositions)
    }

    @Test
    fun replaceQueue_keepsOldItemsWhenNewInsertFails() = runBlocking {
        // Given
        val oldItems = listOf(
            createQueueItem(id = 1, position = 0),
            createQueueItem(id = 2, position = 1)
        )

        queueDao.insertQueueItems(oldItems)

        val invalidNewItems = listOf(
            createQueueItem(id = 3, position = 0),
            createQueueItem(id = 3, position = 1)
        )

        // When
        try {
            queueDao.replaceQueue(invalidNewItems)

            fail("Expected SQLiteConstraintException")
        } catch (_: SQLiteConstraintException) {

        }

        // Then
        val actualIds = queueDao.getQueue().map { item ->
            item.id
        }

        val expectedIds = listOf(1L, 2L)

        assertEquals(expectedIds, actualIds)
    }

    private fun createQueueItem(
        id: Long,
        position: Int
    ): QueueItemEntity {
        return QueueItemEntity(
            id = id,
            songId = "song-$id",
            songUri = "content://songs/$id",
            position = position,
            title = "Song $id",
            artist = "Artist",
            duration = 1_000L,
            album = null,
            genre = null,
            artUri = null
        )
    }

}