package com.example.sound.Data.repository


import com.example.sound.Data.local.playerState.PlayerStateDao
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class PlaybackTransitionRepositoryImplTest {

    private lateinit var playerStateDao: PlayerStateDao
    private lateinit var repository: PlaybackTransitionRepositoryImpl

    @Before
    fun setUp() {
        playerStateDao = mock()
        repository = PlaybackTransitionRepositoryImpl(
            database = mock(),
            playerStateDao = playerStateDao,
            queueDao = mock(),
            defaultQueueDao = mock(),
            editSongDao = mock(),
            genreDao = mock()
        )
    }

    @Test
    fun `genre is normalized`() {
        val result = normalizeGenre("   heavy    metal   ")

        assertThat(result).isEqualTo("Heavy metal")
    }

    @Test
    fun `genre without extra spaces remains valid`() {
        val result = normalizeGenre("Rock")

        assertThat(result).isEqualTo("Rock")
    }

}