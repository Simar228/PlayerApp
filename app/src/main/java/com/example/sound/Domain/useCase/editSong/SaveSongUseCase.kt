package com.example.sound.Domain.useCase.editSong

import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.ImageRepository
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongUiState
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class SaveSongUseCase @Inject constructor(
    private val playbackTransitionRepository: PlaybackTransitionRepository,
    private val imageRepository: ImageRepository,
    private val songRepository: SongRepository,
) {

    suspend operator fun invoke(uiState: MutableStateFlow<EditSongUiState>, song: Song) {
        val state = uiState.value
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

        playbackTransitionRepository.saveInformationEditSong(state.genre, newSong, oldSong)

    }

}