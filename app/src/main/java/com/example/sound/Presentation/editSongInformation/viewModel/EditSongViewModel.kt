package com.example.sound.Presentation.editSongInformation.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.GenreRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EditSongViewModel.Factory::class)
class EditSongViewModel @AssistedInject constructor(
    @Assisted song: Song,
    private val genreRepository: GenreRepository,
) : ViewModel() {

    private val _genreList = MutableStateFlow<List<Genre>>(emptyList())
    val genreList = _genreList.asStateFlow()
    private val _title = MutableStateFlow(song.title.orEmpty())
    val title: StateFlow<String> = _title.asStateFlow()

    private val _artist = MutableStateFlow(song.artist.orEmpty())
    val artist: StateFlow<String> = _artist.asStateFlow()

    private val _album = MutableStateFlow(song.album.orEmpty())
    val album: StateFlow<String> = _album.asStateFlow()

    private val _genre = MutableStateFlow(song.genre.orEmpty())
    val genre = _genre.asStateFlow()

    init {
        viewModelScope.launch {
            _genreList.value = genreRepository.getGenres()
        }
    }

    fun sendEvent(event: EditSongEvent) {
        when (event) {
            is EditSongEvent.EditSongTitle -> _title.value = event.newTitle
            is EditSongEvent.EditSongArtist -> _artist.value = event.newArtist
            is EditSongEvent.EditSongAlbum -> _album.value = event.newAlbum
            is EditSongEvent.EditSongGenre -> _genre.value = event.newGenre
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(userId: Song): EditSongViewModel
    }

}