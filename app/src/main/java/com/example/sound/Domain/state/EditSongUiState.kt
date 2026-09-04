package com.example.sound.Domain.state

import com.example.sound.Domain.model.Genre

data class EditSongUiState(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val genre: String = "",
    val art: String? = null,
    var genres: List<Genre> = emptyList(),
)