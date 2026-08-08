package com.example.sound.Data.local.playerstate

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sound.Data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerStateDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var playerStateDao: PlayerStateDao

    @Before
    fun createDatabase() {
        val context =
            ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        playerStateDao = database.playerStateDao()
    }

    @After
    fun closeDatabase(){
        database.close()
    }

    @Test
    fun savePlayerState_returnsSavedState() = runBlocking {
        // Given
        val expectedState = PlayerStateEntity(
            currentQueueItemId = 42L,
            positionMs = 15_000L
        )

        // When
        playerStateDao.savePlayerState(expectedState)

        val actualState = playerStateDao.getPlayerState()

        // Then
        assertEquals(expectedState, actualState)
    }

    @Test
    fun savePlayerState_replacesPreviousState() = runBlocking {
        // Given
        val firstState = PlayerStateEntity(
            currentQueueItemId = 10L,
            positionMs = 5_000L
        )

        val secondState = PlayerStateEntity(
            currentQueueItemId = 20L,
            positionMs = 25_000L
        )

        // When
        playerStateDao.savePlayerState(firstState)
        playerStateDao.savePlayerState(secondState)

        val actualState = playerStateDao.getPlayerState()

        // Then
        assertEquals(secondState, actualState)
    }

    @Test
    fun clearPlayerState_removesSavedState() = runBlocking {
        // Given
        val savedState = PlayerStateEntity(
            currentQueueItemId = 42L,
            positionMs = 15_000L
        )

        playerStateDao.savePlayerState(savedState)

        // When
        playerStateDao.clearPlayerState()

        val actualState = playerStateDao.getPlayerState()

        // Then
        assertNull(actualState)
    }
}