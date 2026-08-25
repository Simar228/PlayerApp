package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeEditSongRepository : EditSongRepository {
    val setEditSongCalls = mutableListOf<String>()
    var setEditSongResult: Song? = null

    private val editSongsFlow = MutableStateFlow<List<Song>>(emptyList())

    override suspend fun setEditSong(songId: String): Song? {
        setEditSongCalls += songId
        return setEditSongResult
    }

    override fun observeEditSongs(): Flow<List<Song>> = editSongsFlow.asStateFlow()

    fun fakeSetEditSongs(songs: List<Song>) {
        editSongsFlow.value = songs
    }
}
