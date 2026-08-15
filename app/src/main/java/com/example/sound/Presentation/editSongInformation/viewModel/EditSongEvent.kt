package com.example.sound.Presentation.editSongInformation.viewModel

sealed interface EditSongEvent{

    data class EditSongTitle(val newTitle: String) : EditSongEvent
    data class EditSongArtist(val newArtist: String) : EditSongEvent
    data class EditSongAlbum(val newAlbum: String) : EditSongEvent
    data class EditSongGenre(val newGenre: String) : EditSongEvent


}