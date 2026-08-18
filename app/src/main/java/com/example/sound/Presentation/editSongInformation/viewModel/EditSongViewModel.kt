package com.example.sound.Presentation.editSongInformation.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.GenreRepository
import com.example.sound.Domain.repository.ImageRepository
import com.example.sound.Domain.repository.PlaybackTransitionRepository
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
    @Assisted private val song: Song,
    private val imageRepository: ImageRepository,
    private val genreRepository: GenreRepository,
    private val playbackTransitionRepository: PlaybackTransitionRepository
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
    private val _art = MutableStateFlow(song.art)
    val art = _art.asStateFlow()

    init {
        viewModelScope.launch {
            _genreList.value = genreRepository.getGenres()
        }
    }

    fun setArt(newArt: String) {
        _art.value = newArt
    }

    fun sendEvent(event: EditSongEvent) {
        when (event) {
            is EditSongEvent.EditSongTitle -> _title.value = event.newTitle
            is EditSongEvent.EditSongArtist -> _artist.value = event.newArtist
            is EditSongEvent.EditSongAlbum -> _album.value = event.newAlbum
            is EditSongEvent.EditSongGenre -> _genre.value = event.newGenre
        }
    }

    fun saveSong() {
        viewModelScope.launch {
            var fileUri: String? = null
            _art.value?.let { art ->
                fileUri = imageRepository.saveImage(art)
            }

            val newSong = Song(
                id = song.id,
                title = _title.value,
                artist = _artist.value,
                duration = song.duration,
                uri = song.uri,
                album = _album.value,
                genre = _genre.value,
                art = fileUri
            )
            playbackTransitionRepository.saveInformationEditSong(_genre.value, newSong)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(userId: Song): EditSongViewModel
    }

}