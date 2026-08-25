package com.example.sound.Data.repository

import com.example.sound.Data.local.editSong.EditSongDao
import com.example.sound.Data.local.editSong.toSong
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.EditSongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EditSongRepositoryImpl @Inject constructor(
    val editSongDao: EditSongDao,
) : EditSongRepository {

    override suspend fun setEditSong(songId: String): Song? = withContext(Dispatchers.IO) {
        val editSongItemEntity = editSongDao.setEditSong(songId)
        editSongItemEntity?.let {
            return@withContext Song(
                id = editSongItemEntity.songId,
                title = editSongItemEntity.oldSongTitle,
                artist = editSongItemEntity.oldSongArtist,
                duration = editSongItemEntity.songDuration,
                uri = editSongItemEntity.songUri,
                album = editSongItemEntity.oldSongAlbum,
                genre = editSongItemEntity.oldSongGenre,
                art = editSongItemEntity.oldSongImagePath
            )
        }
        return@withContext null
    }


    override fun observeEditSongs(): Flow<List<Song>> {
        val songs = editSongDao.observeEditSong().map { editSongItemEntities ->
            editSongItemEntities.map { editSongItemEntity ->
                editSongItemEntity.toSong()
            }
        }
        return songs
    }
}