package com.example.sound.Data.editSong

import com.example.sound.Data.local.editSong.EditSongDao
import com.example.sound.Data.local.editSong.EditSongItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeEditSongDao : EditSongDao {

    private val editSongList = MutableStateFlow<List<EditSongItemEntity>>(emptyList())

    override suspend fun getBySongId(id: String): EditSongItemEntity? {
        return editSongList.value.find { it.songId == id }
    }

    override suspend fun deleteBySongId(id: String) {
        val newList = editSongList.value.filterNot { it.songId == id }
        editSongList.value = newList
    }

    override suspend fun addEditSong(editSongItemEntity: EditSongItemEntity) {
        editSongList.value += editSongItemEntity
    }

    override fun observeEditSong(): Flow<List<EditSongItemEntity>> {
        return editSongList
    }
}