package com.example.sound.Presentation.editSongInformation.viewModel


import androidx.lifecycle.ViewModel
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditSongViewModel(song: Song)
    : ViewModel() {

    private val _title = MutableStateFlow(song.title.orEmpty())
    val title: StateFlow<String> = _title.asStateFlow()

    private val _artist = MutableStateFlow(song.artist.orEmpty())
    val artist: StateFlow<String> = _artist.asStateFlow()

    private val _album = MutableStateFlow(song.album.orEmpty())
    val album: StateFlow<String> = _album.asStateFlow()

    private val _genre = MutableStateFlow(song.genre.orEmpty())
    val genre: StateFlow<String> = _genre.asStateFlow()


    fun sendEvent(event: EditSongEvent) {
        when (event) {
            is EditSongEvent.EditSongTitle -> _title.value = event.newTitle
            is EditSongEvent.EditSongArtist -> _artist.value = event.newArtist
            is EditSongEvent.EditSongAlbum -> _album.value = event.newAlbum
//            is EditSongEvent.EditSongGenre -> _genre.value = event.newGenre
        }
    }

}