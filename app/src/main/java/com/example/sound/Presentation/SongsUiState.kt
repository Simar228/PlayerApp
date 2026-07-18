package com.example.sound.Presentation

import com.example.sound.Domain.model.Song

sealed interface SongsUiState {
    data object Loading : SongsUiState

    data object PermissionDenied : SongsUiState

    data class Success(
        val songs: List<Song>
    ) : SongsUiState

    data class Error(
        val message: String
    ) : SongsUiState
}