package com.example.sound.Domain.useCase.mainActivity

import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.SongsUiState
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class LoadSongUseCase @Inject constructor(
    private val songRepository: SongRepository
) {

    suspend operator fun invoke(songsUiState: MutableStateFlow<SongsUiState>) {
        if (songsUiState.value is SongsUiState.PermissionDenied) {
            return
        }
        songsUiState.value = SongsUiState.Loading

        try {
            withTimeout(30_000L) {
                songRepository.loadSongs()
                songRepository.songs.first { songs ->
                    songs.isNotEmpty()
                }
            }
            songsUiState.value = SongsUiState.Success
        } catch (exception: TimeoutCancellationException) {
            songsUiState.value = SongsUiState.Error(
                message = "Не удалось загрузить песни: превышено время ожидания"
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            songsUiState.value = SongsUiState.Error(exception.toString())
        }
    }
}
