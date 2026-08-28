package com.example.sound.Domain.useCase.editSong

import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.EditSongRepository
import com.example.sound.Domain.repository.GenreRepository
import com.example.sound.Domain.repository.ImageRepository
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongUiState
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SaveSongUseCase @Inject constructor(
    private val imageRepository: ImageRepository,
    private val songRepository: SongRepository,
    private val genreRepository: GenreRepository,
    private val editSongRepository: EditSongRepository,
) {

    suspend operator fun invoke(uiState: MutableStateFlow<EditSongUiState>, song: Song) {

        val correctGenre = normalizeGenre(uiState.value.genre)

        val state = uiState.value.copy(genre = correctGenre)

        var fileUri: String? = null
        state.art?.let { art ->
            fileUri = imageRepository.saveImage(art)
        }
        val oldSong = songRepository.originalSongs.value.find { it.id == song.id } ?: song

        val newSong = Song(
            id = song.id,
            title = state.title,
            artist = state.artist,
            duration = song.duration,
            uri = song.uri,
            album = state.album,
            genre = state.genre,
            art = fileUri
        )


        genreRepository.insertGenre(correctGenre)
        editSongRepository.insertEditSong(newSong, oldSong)

    }

    private fun normalizeGenre(genre: String): String {
        return genre
            .trim()
            .replace(Regex("\\s+"), " ")
            .replaceFirstChar { it.uppercase() }
    }
}