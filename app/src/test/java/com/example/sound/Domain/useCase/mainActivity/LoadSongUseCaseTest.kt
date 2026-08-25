package com.example.sound.Domain.useCase.mainActivity

import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.repository.FakeSongRepository
import com.example.sound.Presentation.SongsUiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class LoadSongUseCaseTest {

    private lateinit var sut: LoadSongUseCase
    private lateinit var repository: FakeSongRepository
    private lateinit var songsUiState: MutableStateFlow<SongsUiState>

    @Before
    fun setUp() {
        repository = FakeSongRepository()
        sut = LoadSongUseCase(repository)
        songsUiState = MutableStateFlow(SongsUiState.Loading)
    }

    @Test
    fun `invoke does nothing when permission is denied`() = runTest {
        songsUiState.value = SongsUiState.PermissionDenied

        sut(songsUiState)

        assertThat(repository.loadSongsCallCount).isEqualTo(0)
        assertThat(songsUiState.value).isEqualTo(SongsUiState.PermissionDenied)
    }

    @Test
    fun `invoke updates ui state to success when songs are loaded`() = runTest {
        repository.songsToLoad = listOf(FakeSong.SONG_0)

        sut(songsUiState)

        assertThat(repository.loadSongsCallCount).isEqualTo(1)
        assertThat(songsUiState.value).isEqualTo(SongsUiState.Success)
    }

    @Test
    fun `invoke updates ui state to error when repository throws exception`() = runTest {
        val exception = IllegalStateException("Songs loading failed")
        repository.loadSongsException = exception

        sut(songsUiState)

        assertThat(songsUiState.value).isEqualTo(
            SongsUiState.Error(exception.toString())
        )
    }

    @Test
    fun `invoke rethrows cancellation exception`() = runTest {
        val cancellationException = CancellationException("Cancelled")
        repository.loadSongsException = cancellationException

        val thrownException = runCatching {
            sut(songsUiState)
        }.exceptionOrNull()

        assertThat(thrownException).isInstanceOf(CancellationException::class.java)
        assertThat(thrownException).hasMessageThat().isEqualTo("Cancelled")
    }

    @Test
    fun `invoke updates ui state to timeout error after 30 seconds`() = runTest {
        val loadJob = launch {
            sut(songsUiState)
        }
        runCurrent()

        advanceTimeBy(29_999L)
        runCurrent()

        assertThat(songsUiState.value).isEqualTo(SongsUiState.Loading)
        assertThat(loadJob.isActive).isTrue()

        advanceTimeBy(1L)
        runCurrent()

        assertThat(songsUiState.value).isEqualTo(
            SongsUiState.Error(
                message = "Не удалось загрузить песни: превышено время ожидания"
            )
        )
        assertThat(loadJob.isCompleted).isTrue()
    }
}
