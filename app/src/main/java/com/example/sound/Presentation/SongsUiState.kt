package com.example.sound.Presentation

sealed interface SongsUiState {
    data object Loading : SongsUiState

    data object PermissionDenied : SongsUiState

    data object Success : SongsUiState

    data class Error(
        val message: String
    ) : SongsUiState
}