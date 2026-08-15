package com.example.sound.Data.local.playerState

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
        val expectedState = createPlayerState(songId = "42")

        // When
        playerStateDao.savePlayerState(expectedState)

        val actualState = playerStateDao.getPlayerState()

        // Then
        assertEquals(expectedState, actualState)
    }

    @Test
    fun savePlayerState_replacesPreviousState() = runBlocking {
        // Given
        val firstState = createPlayerState(songId = "first")
        val secondState = createPlayerState(songId = "second")

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
        val savedState = createPlayerState(songId = "42")

        playerStateDao.savePlayerState(savedState)

        // When
        playerStateDao.clearPlayerState()

        val actualState = playerStateDao.getPlayerState()

        // Then
        assertNull(actualState)
    }

    private fun createPlayerState(songId: String) = PlayerStateEntity(
        currentSongId = songId,
        currentSongUri = "content://song/$songId",
        currentSongTitle = "Song $songId",
        currentSongArtist = "Artist",
        currentSongDuration = 1_000L,
        currentSongAlbum = null,
        currentSongGenre = null,
        currentSongArtUri = null,
    )
}
