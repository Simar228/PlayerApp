package com.example.sound.Presentation.editSongInformation.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.EditSongRepository
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
    private val editSongRepository: EditSongRepository,
    private val imageRepository: ImageRepository,
    private val genreRepository: GenreRepository,
    private val playbackTransitionRepository: PlaybackTransitionRepository
) : ViewModel() {

    private var edited = false
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
        edited = true
        _art.value = newArt
    }


    fun sendEvent(event: EditSongEvent) {
        when (event) {
            is EditSongEvent.EditSongTitle -> {
                if (_title.value != event.newTitle) {
                    _title.value = event.newTitle
                    edited = true
                }
            }

            is EditSongEvent.EditSongArtist -> {
                if (_artist.value != event.newArtist) {
                    _artist.value = event.newArtist
                    edited = true
                }
            }

            is EditSongEvent.EditSongAlbum -> {
                if (_album.value != event.newAlbum) {
                    _album.value = event.newAlbum
                    edited = true
                }
            }

            is EditSongEvent.EditSongGenre -> {
                if (_genre.value != event.newGenre) {
                    _genre.value = event.newGenre
                    edited = true
                }
            }
        }
    }

    fun setSong() {
        edited = false
        viewModelScope.launch {
            editSongRepository.setEditSong(song.id)?.let { oldSong ->
                _art.value = oldSong.art
                _title.value = oldSong.title.orEmpty()
                _artist.value = oldSong.artist.orEmpty()
                _album.value = oldSong.album.orEmpty()
                _genre.value = oldSong.genre.orEmpty()
            }
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
            if (edited) {
                playbackTransitionRepository.saveInformationEditSong(_genre.value, newSong, song)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(userId: Song): EditSongViewModel
    }

}