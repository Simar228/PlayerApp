package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSongRepository(
    var songsToLoad: List<Song> = emptyList(),

    ) : SongRepository {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    private val _originalSongs = MutableStateFlow<List<Song>>(emptyList())

    override val songs = _songs.asStateFlow()
    override val originalSongs = _originalSongs.asStateFlow()

    var loadSongsCallCount: Int = 0
        private set
    var loadSongsException: Throwable? = null

    override fun loadSongs() {
        loadSongsCallCount++
        loadSongsException?.let { exception -> throw exception }
        _songs.value = songsToLoad
    }

    fun setOriginalSongs(songs: List<Song>){
        _originalSongs.value = songs
    }
}
