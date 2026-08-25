package com.example.sound.Presentation.editSongInformation.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.GenreRepository
import com.example.sound.Domain.useCase.editSong.SaveSongUseCase
import com.example.sound.Domain.useCase.editSong.SetSongUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditSongViewModel.Factory::class)
class EditSongViewModel @AssistedInject constructor(
    @Assisted private val song: Song,
    private val setSongUseCase: SetSongUseCase,
    private val saveSongUseCase: SaveSongUseCase,
    private val genreRepository: GenreRepository,
) : ViewModel() {

    private var edited = false
    private val _uiState = MutableStateFlow(
        EditSongUiState(
            title = song.title.orEmpty(),
            artist = song.artist.orEmpty(),
            album = song.album.orEmpty(),
            genre = song.genre.orEmpty(),
            art = song.art,
        )
    )
    val uiState: StateFlow<EditSongUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(genres = genreRepository.getGenres()) }
        }
    }

    fun setArt(newArt: String) {
        edited = true
        _uiState.update { state -> state.copy(art = newArt) }
    }


    fun sendEvent(event: EditSongEvent) {
        when (event) {
            is EditSongEvent.EditSongTitle -> {
                if (_uiState.value.title != event.newTitle) {
                    _uiState.update { state -> state.copy(title = event.newTitle) }
                    edited = true
                }
            }

            is EditSongEvent.EditSongArtist -> {
                if (_uiState.value.artist != event.newArtist) {
                    _uiState.update { state -> state.copy(artist = event.newArtist) }
                    edited = true
                }
            }

            is EditSongEvent.EditSongAlbum -> {
                if (_uiState.value.album != event.newAlbum) {
                    _uiState.update { state -> state.copy(album = event.newAlbum) }
                    edited = true
                }
            }

            is EditSongEvent.EditSongGenre -> {
                if (_uiState.value.genre != event.newGenre) {
                    _uiState.update { state -> state.copy(genre = event.newGenre) }
                    edited = true
                }
            }
        }
    }

    fun setSong() {
        edited = false
        viewModelScope.launch {
            setSongUseCase(_uiState, song)
        }
    }

    fun saveSong() {
        viewModelScope.launch {
            if (edited) {
                saveSongUseCase(_uiState, song)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(userId: Song): EditSongViewModel
    }

}
