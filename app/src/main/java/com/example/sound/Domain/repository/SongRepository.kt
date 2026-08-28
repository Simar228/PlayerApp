package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.StateFlow

interface SongRepository {
    val songs: StateFlow<List<Song>>
    val originalSongs: StateFlow<List<Song>>

    fun loadSongs()

}
