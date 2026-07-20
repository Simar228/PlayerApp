package com.example.sound.Presentation.mainScreen

sealed interface MainSortScreenEvents {
    data class SortByArtist(val isUp: Boolean): MainSortScreenEvents
    data class SortByTitle(val isUp: Boolean): MainSortScreenEvents
    data class SortByAlbum(val isUp: Boolean): MainSortScreenEvents
    data class SortByGenre(val isUp: Boolean): MainSortScreenEvents
}

