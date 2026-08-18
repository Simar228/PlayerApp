package com.example.sound.Data.local.editSong

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.Song


@Entity(
    tableName = DatabaseTableNames.EDIT_SONG,
    indices = [
        Index(
            value = ["editSongId"],
            unique = true
        )
    ]
)
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
    val editSongImagePath: String?
)

fun Song.toEditSongItemEntity(): EditSongItemEntity{
    return EditSongItemEntity(
        editSongId = id,
        editSongTitle = title,
        editSongArtist = artist,
        editSongDuration = duration,
        editSongUri = uri,
        editSongAlbum = album,
        editSongGenre = genre,
        editSongImagePath = art
    )
}

fun EditSongItemEntity.toSong(): Song{
    return Song(
        id = editSongId,
        title = editSongTitle,
        artist = editSongArtist,
        duration = editSongDuration,
        uri = editSongUri,
        album = editSongAlbum,
        genre = editSongGenre,
        art = editSongImagePath
    )
}