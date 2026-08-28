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

    val originalSongTitle: String? = null,
    val originalSongArtist: String? = null,
    val originalSongAlbum: String? = null,
    val originalSongGenre: String? = null,
    val originalSongImagePath: String? = null,

    )

fun Song.toEditSongItemEntity(oldSong: Song): EditSongItemEntity {
    return EditSongItemEntity(
        songId = id,
        editSongTitle = title,
        editSongArtist = artist,
        songDuration = duration,
        songUri = uri,
        editSongAlbum = album,
        editSongGenre = genre,
        editSongImagePath = art,

        originalSongTitle = oldSong.title,
        originalSongArtist = oldSong.artist,
        originalSongAlbum = oldSong.album,
        originalSongGenre = oldSong.genre,
        originalSongImagePath = oldSong.art
    )
}

fun EditSongItemEntity.toNewSong(): Song {
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

fun EditSongItemEntity.toOriginalSong(): Song {
    return Song(
        id = this.songId,
        title = this.originalSongTitle,
        artist = this.originalSongArtist,
        duration = this.songDuration,
        uri = this.songUri,
        album = this.originalSongAlbum,
        genre = this.originalSongGenre,
        art = this.originalSongImagePath
    )
}