package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow


interface EditSongRepository {
    suspend fun insertEditSong(newSong: Song, oldSong: Song)
    suspend fun setEditSong(songId: String): Song?

    fun observeEditSongs(): Flow<List<Song>>

}