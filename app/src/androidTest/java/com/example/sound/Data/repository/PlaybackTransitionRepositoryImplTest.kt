package com.example.sound.Data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.defualtQueue.toSong
import com.example.sound.Data.local.playerState.toDomain
import com.example.sound.Data.local.queue.toDomain
import com.example.sound.Domain.model.QueueItem
import com.example.sound.testing.createTestQueueItem
import com.example.sound.testing.createTestSong
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackTransitionRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: PlaybackTransitionRepositoryImpl

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        repository = PlaybackTransitionRepositoryImpl(
            database = database,
            playerStateDao = database.playerStateDao(),
            queueDao = database.queueDao(),
            defaultQueueDao = database.defaultQueueDao(),
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun startPlaybackUpdatesDefaultQueuePlayerStateAndQueueInOneTransaction() = runBlocking {
        // Given
        val songA = createTestSong(id = "A")
        val songB = createTestSong(id = "B")
        val songC = createTestSong(id = "C")
        val defaultQueueSongs = listOf(songC, songA, songB)
        val initialQueue = listOf(
            createTestQueueItem(id = 1L, song = songA, position = 0),
            createTestQueueItem(id = 2L, song = songB, position = 1),
            createTestQueueItem(id = 3L, song = songC, position = 2),
        )
        database.queueDao().insertQueueItems(initialQueue)

        // When
        repository.startPlayback(
            song = songB,
            defaultQueueSongs = defaultQueueSongs,
            queueItemId = 2L,
        )

        // Then: DefaultQueue contains every song in the requested order
        val actualDefaultQueueSongs = database.defaultQueueDao()
            .getDefaultQueue()
            .map { entity -> entity.toSong() }
        assertEquals(defaultQueueSongs, actualDefaultQueueSongs)

        // Then: selected song is saved as the current player state
        val actualCurrentSong = database.playerStateDao()
            .getPlayerState()
            ?.toDomain()
            ?.currentSong
        assertEquals(songB, actualCurrentSong)

        // Then: played queue item is removed and remaining items are reindexed
        val actualQueue = database.queueDao()
            .getQueue()
            .map { entity -> entity.toDomain() }
        assertEquals(
            listOf(
                QueueItem(id = 1L, song = songA, position = 0),
                QueueItem(id = 3L, song = songC, position = 1),
            ),
            actualQueue,
        )
    }
}
