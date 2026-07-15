package com.example.sound.Presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Routes {

    @Serializable
    data object MainGraph: Routes
    @Serializable
    data object SongsRoute : Routes
    @Serializable
    data object AlbumsRoute : Routes
    @Serializable
    data object QueueRoute : Routes
    @Serializable
    data object SongPageRoute: Routes
}
