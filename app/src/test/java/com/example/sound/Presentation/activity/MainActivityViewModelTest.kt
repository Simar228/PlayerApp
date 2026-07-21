package com.example.sound.Presentation.activity

import android.net.Uri
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.SongsUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class MainActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadSongs retries after error`() = runTest {
        //Given
        val exception = RuntimeException("Test loading error")
        val expectedSongs = listOf(
            createSong("1", "Teddy")
        )
        val repository = mock(SongRepository::class.java)
        `when`(repository.getSong())
            .thenThrow(exception)
            .thenReturn(expectedSongs)

        val viewModel = MainActivityViewModel(repository)

        //When
        viewModel.loadSongs()
        viewModel.songsUiState.first { state ->
            state is SongsUiState.Error
        }
        viewModel.loadSongs()
        val actualSuccessState = viewModel.songsUiState.first { state ->
            state is SongsUiState.Success
        }
        val expectedState = SongsUiState.Success(expectedSongs)

        //Then
        assertEquals(expectedState, actualSuccessState)
        verify(repository, times(2)).getSong()
    }


    @Test
    fun `loadSongs does not reload after success`() = runTest {
        //Given
        val expectedSongs = listOf(
            createSong("1", "Teddy")
        )
        val repository = mock(SongRepository::class.java)
        `when`(repository.getSong())
            .thenReturn(expectedSongs)
        val viewModel = MainActivityViewModel(repository)


        //When
        viewModel.loadSongs()
        viewModel.songsUiState.first { state ->
            state is SongsUiState.Success
        }
        viewModel.loadSongs()

        //Then
        verify(repository, times(1)).getSong()
    }

    @Test
    fun `permission denial updates state`() {
        // Given
        val repository = mock(SongRepository::class.java)
        val viewModel = MainActivityViewModel(repository)

        // When
        viewModel.permissionDenied()

        // Then
        val actualState = viewModel.songsUiState.value
        val expectedState = SongsUiState.PermissionDenied

        assertEquals(expectedState, actualState)
    }

    @Test
    fun `repository failure updates state with error`() = runTest {
        //Given
        val exception = RuntimeException("Test loading error")
        val repository = mock(SongRepository::class.java)
        `when`(repository.getSong())
            .thenThrow(exception)
        val viewModel = MainActivityViewModel(repository)

        //When
        viewModel.loadSongs()

        //Then
        val actualState = viewModel.songsUiState.first { state ->
            state is SongsUiState.Error
        }

        val expectedException = SongsUiState.Error(exception.toString())

        assertEquals(expectedException, actualState)
    }

    @Test
    fun `successful loading updates state with songs`() = runTest {
        // Given
        val expectedSongs = listOf(
            createSong(id = "1", title = "Alpha")
        )

        val repository = mock(SongRepository::class.java)

        `when`(repository.getSong())
            .thenReturn(expectedSongs)

        val viewModel = MainActivityViewModel(repository)

        // When
        viewModel.loadSongs()

        // Then
        val actualState = viewModel.songsUiState.first { state ->
            state is SongsUiState.Success
        }

        val expectedState = SongsUiState.Success(expectedSongs)

        assertEquals(expectedState, actualState)
    }

    private fun createSong(
        id: String,
        title: String
    ): Song {
        return Song(
            id = id,
            title = title,
            artist = null,
            duration = 0L,
            uri = mock(Uri::class.java),
            album = null,
            genre = null,
            art = null
        )
    }

}