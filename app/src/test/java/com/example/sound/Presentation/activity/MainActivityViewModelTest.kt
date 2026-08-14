package com.example.sound.Presentation.activity

import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.SongsUiState
import com.example.sound.testing.createTestSong
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MainActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun successfulLoadingUpdatesStateWithSongs() = runTest {
        val expectedSongs = listOf(
            createTestSong(id = "1", title = "Alpha"),
            createTestSong(id = "2", title = "Beta"),
        )
        val repository = mock<SongRepository>()
        whenever(repository.observeSongs()).thenReturn(flowOf(expectedSongs))
        val viewModel = MainActivityViewModel(repository)

        viewModel.loadSongs()

        assertEquals(SongsUiState.Success(expectedSongs), viewModel.songsUiState.value)
        verify(repository, times(1)).observeSongs()
    }

    @Test
    fun loadSongsAfterPermissionDenialDoesNotObserveRepository() = runTest {
        val repository = mock<SongRepository>()
        val viewModel = MainActivityViewModel(repository)

        viewModel.permissionDenied()
        viewModel.loadSongs()

        assertEquals(SongsUiState.PermissionDenied, viewModel.songsUiState.value)
        verify(repository, never()).observeSongs()
    }

    @Test
    fun setSongUiStateResetsStateToLoading() {
        val repository = mock<SongRepository>()
        val viewModel = MainActivityViewModel(repository)

        assertEquals(SongsUiState.Loading, viewModel.songsUiState.value)
        viewModel.permissionDenied()
        viewModel.setSongUiState()

        assertEquals(SongsUiState.Loading, viewModel.songsUiState.value)
    }

    @Test
    fun loadSongsRetriesSuccessfullyAfterRepositoryFailure() = runTest {
        val exception = RuntimeException("Test loading error")
        val expectedSongs = listOf(
            createTestSong(id = "1"),
            createTestSong(id = "2"),
        )
        val repository = mock<SongRepository>()
        whenever(repository.observeSongs())
            .thenReturn(flow<List<Song>> { throw exception })
            .thenReturn(flowOf(expectedSongs))
        val viewModel = MainActivityViewModel(repository)

        viewModel.loadSongs()
        assertEquals(SongsUiState.Error(exception.toString()), viewModel.songsUiState.value)

        viewModel.loadSongs()
        assertEquals(SongsUiState.Success(expectedSongs), viewModel.songsUiState.value)
        verify(repository, times(2)).observeSongs()
    }

    @Test
    fun repositoryFailureUpdatesStateWithError() = runTest {
        val exception = RuntimeException("Test loading error")
        val repository = mock<SongRepository>()
        whenever(repository.observeSongs())
            .thenReturn(
            flow<List<Song>> { throw exception }
        )
        val viewModel = MainActivityViewModel(repository)

        viewModel.loadSongs()

        assertEquals(SongsUiState.Error(exception.toString()), viewModel.songsUiState.value)
        verify(repository, ).observeSongs()
    }

    @Test
    fun permissionDenialUpdatesState() {
        val repository = mock<SongRepository>()
        val viewModel = MainActivityViewModel(repository)

        viewModel.permissionDenied()

        assertEquals(SongsUiState.PermissionDenied, viewModel.songsUiState.value)
    }
}
