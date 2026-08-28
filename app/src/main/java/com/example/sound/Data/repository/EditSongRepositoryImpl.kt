package com.example.sound.Data.repository

import com.example.sound.Data.local.editSong.EditSongDao
import com.example.sound.Data.local.editSong.toNewSong
import com.example.sound.Data.local.editSong.toOriginalSong
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.EditSongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EditSongRepositoryImpl @Inject constructor(
    val editSongDao: EditSongDao,
) : EditSongRepository {

    override suspend fun setEditSong(songId: String): Song? {
        val editSongItemEntity = editSongDao.setEditSong(songId)
        editSongItemEntity?.let {
            return editSongItemEntity.toOriginalSong()
        }
        return null
    }


    override fun observeEditSongs(): Flow<List<Song>> {
        val songs = editSongDao.observeEditSong().map { editSongItemEntities ->
            editSongItemEntities.map { editSongItemEntity ->
                editSongItemEntity.toNewSong()
            }
        }
        return songs
    }
}