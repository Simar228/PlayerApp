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
            value = ["songId"],
            unique = true
        )
    ]
)
data class EditSongItemEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val songId: String,

    val editSongTitle: String?,
    val editSongArtist: String?,
    val songDuration: Long,
    val songUri: String,
    val editSongAlbum: String?,
    val editSongGenre: String?,
    val editSongImagePath: String?,

    val oldSongTitle: String? = null,
    val oldSongArtist: String? = null,
    val oldSongAlbum: String? = null,
    val oldSongGenre: String? = null,
    val oldSongImagePath: String? = null,

)

fun Song.toEditSongItemEntity(oldSong: Song): EditSongItemEntity{
    return EditSongItemEntity(
        songId = id,
        editSongTitle = title,
        editSongArtist = artist,
        songDuration = duration,
        songUri = uri,
        editSongAlbum = album,
        editSongGenre = genre,
        editSongImagePath = art,

        oldSongTitle = oldSong.title,
        oldSongArtist = oldSong.artist,
        oldSongAlbum = oldSong.album,
        oldSongGenre = oldSong.genre,
        oldSongImagePath = oldSong.art
    )
}

fun EditSongItemEntity.toSong(): Song{
    return Song(
        id = songId,
        title = editSongTitle,
        artist = editSongArtist,
        duration = songDuration,
        uri = songUri,
        album = editSongAlbum,
        genre = editSongGenre,
        art = editSongImagePath
    )
}