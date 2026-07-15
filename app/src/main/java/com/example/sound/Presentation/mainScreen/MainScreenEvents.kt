package com.example.sound.Presentation.mainScreen

sealed interface MainScreenEvents {
    data class SortByArtist(val isUp: Boolean): MainScreenEvents
    data class SortByTitle(val isUp: Boolean): MainScreenEvents
    data class SortByAlbum(val isUp: Boolean): MainScreenEvents
    data class SortByGenre(val isUp: Boolean): MainScreenEvents
}

