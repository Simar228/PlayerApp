package com.example.sound.Domain.useCase.editSong

import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.EditSongRepository
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class SetSongUseCase @Inject constructor(
    private val editSongRepository: EditSongRepository,
) {
    suspend operator fun invoke(uiState: MutableStateFlow<EditSongUiState>, song: Song) {
        editSongRepository.setEditSong(song.id)?.let { oldSong ->
            uiState.update { state ->
                state.copy(
                    title = oldSong.title.orEmpty(),
                    artist = oldSong.artist.orEmpty(),
                    album = oldSong.album.orEmpty(),
                    genre = oldSong.genre.orEmpty(),
                    art = oldSong.art,
                )
            }
        }
    }
}

