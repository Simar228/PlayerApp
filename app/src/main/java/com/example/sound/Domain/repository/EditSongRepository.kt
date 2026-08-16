package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow


interface EditSongRepository {

    suspend fun addEditSong(song: Song,)
    suspend fun observeEditSongs(): Flow<List<Song>>

}