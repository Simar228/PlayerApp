package com.example.sound.Data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.defualtQueue.toDefaultQueueEntity
import com.example.sound.Data.local.playerstate.toPlayerStateEntity
import com.example.sound.Data.local.queue.QueueItemEntity
import com.example.sound.Domain.model.PlaybackQueueState
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import com.example.sound.testing.createTestSong
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackQueueStateRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: PlaybackQueueStateRepositoryImpl

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        repository = PlaybackQueueStateRepositoryImpl(
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
    fun observePlaybackQueueStateUpdatesWhenTrackedTablesChange() = runBlocking {
        val observedStates = repository
            .observePlaybackQueueState()
            .produceIn(this)

        try {
            assertEquals(
                PlaybackQueueState(
                    currentSong = null,
                    queueItems = emptyList(),
                    defaultQueueSongs = emptyList(),
                ),
                observedStates.awaitState(),
            )

            val currentSong = createTestSong(id = "current")
            database.playerStateDao().savePlayerState(
                currentSong.toPlayerStateEntity()
            )

            assertEquals(
                PlaybackQueueState(
                    currentSong = currentSong,
                    queueItems = emptyList(),
                    defaultQueueSongs = emptyList(),
                ),
                observedStates.awaitState(),
            )

            val queuedSong = createTestSong(id = "queued")
            val queueItem = QueueItem(
                id = 1L,
                song = queuedSong,
                position = 0,
            )
            database.queueDao().insertQueueItem(
                QueueItemEntity(
                    id = queueItem.id,
                    songId = queuedSong.id,
                    songUri = queuedSong.uri,
                    position = queueItem.position,
                    title = queuedSong.title,
                    artist = queuedSong.artist,
                    duration = queuedSong.duration,
                    album = queuedSong.album,
                    genre = queuedSong.genre,
                    artUri = queuedSong.art,
                )
            )

            assertEquals(
                PlaybackQueueState(
                    currentSong = currentSong,
                    queueItems = listOf(queueItem),
                    defaultQueueSongs = emptyList(),
                ),
                observedStates.awaitState(),
            )

            val defaultSong = createTestSong(id = "default")
            database.defaultQueueDao().insertDefaultQueueItems(
                listOf(defaultSong.toDefaultQueueEntity(position = 0))
            )

            assertEquals(
                PlaybackQueueState(
                    currentSong = currentSong,
                    queueItems = listOf(queueItem),
                    defaultQueueSongs = listOf(defaultSong),
                ),
                observedStates.awaitState(),
            )
        } finally {
            observedStates.cancel()
        }
    }

    private suspend fun ReceiveChannel<PlaybackQueueState>.awaitState(): PlaybackQueueState {
        return withTimeout(5_000L) {
            receive()
        }
    }
}
