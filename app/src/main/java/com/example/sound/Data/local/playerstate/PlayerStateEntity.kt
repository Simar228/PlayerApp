package com.example.sound.Data.local.playerstate

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song
import androidx.core.net.toUri


@Entity(tableName = "player_state")
data class PlayerStateEntity(
    @PrimaryKey
    val id: Int = 0,

    val currentSongId: String,
    val currentSongUri: String,
    val currentSongTitle: String,
    val currentSongArtist: String?,
    val currentSongDuration: Long,
    val currentSongAlbum: String?,
    val currentSongGenre: String?,
    val currentSongArtUri: String?
)

fun PlayerState.toEntity(): PlayerStateEntity{
    return PlayerStateEntity(
        currentSongId = currentSong?.id ?: "null",
        currentSongUri = currentSong?.uri.toString(),
        currentSongTitle = currentSong?.title ?: "null",
        currentSongArtist = currentSong?.artist,
        currentSongDuration = currentSong?.duration ?: 0,
        currentSongAlbum = currentSong?.album,
        currentSongGenre = currentSong?.genre,
        currentSongArtUri = currentSong?.art.toString(),
    )
}

fun PlayerStateEntity.toDomain(): PlayerState{
    return PlayerState(
        currentSong = if (currentSongId == "null") null else Song(
            id = currentSongId,
            title = currentSongTitle,
            artist = currentSongArtist,
            duration = currentSongDuration,
            uri = currentSongUri.toUri(),
            album = currentSongAlbum,
            genre = currentSongGenre,
            art = currentSongArtUri?.toUri()
        )
    )
}