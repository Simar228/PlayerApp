package com.example.sound.Presentation.mainScreen

sealed interface MainNavigationEvents {

    data class OpenSongMenuBottomSheet(val songId: String): MainNavigationEvents

}