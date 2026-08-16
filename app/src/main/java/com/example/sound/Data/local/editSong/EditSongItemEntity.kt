package com.example.sound.Data.local.editSong

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.Song


@Entity(tableName = DatabaseTableNames.EDIT_SONG)
data class EditSongItemEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val editSongId: String,
    val editSongTitle: String?,
    val editSongArtist: String?,
    val editSongDuration: Long,
    val editSongUri: String,
    val editSongAlbum: String?,
    val editSongGenre: String?,
    val editSongImageId: String?
)