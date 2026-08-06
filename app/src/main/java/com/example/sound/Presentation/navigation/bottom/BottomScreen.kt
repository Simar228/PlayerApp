package com.example.sound.Presentation.navigation.bottom

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.sound.Presentation.navigation.Routes

sealed class BottomScreen(
    val route: Routes,
    val title: String,
    val icon: ImageVector
) {

    data object Songs : BottomScreen(
        route = Routes.SongsRoute,
        title = "Песни",
        icon = Icons.Default.MusicNote
    )


    data object Albums : BottomScreen(
        route = Routes.AlbumsRoute,
        title = "Альбомы",
        icon = Icons.Default.LibraryMusic
    )


    data object Queue : BottomScreen(
        route = Routes.QueueRoute,
        title = "Очередь",
        icon = Icons.AutoMirrored.Filled.QueueMusic
    )
    companion object {
        val items = listOf(
            Songs,
            Albums,
            Queue,
        )
    }
}
