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

    val positionMs: Long?,
    val defaultQueue: Boolean,

    val currentSongId: String?,
    val currentSongUri: String?,
    val currentSongTitle: String?,
    val currentSongArtist: String?,
    val currentSongDuration: Long?,
    val currentSongAlbum: String?,
    val currentSongGenre: String?,
    val currentSongArtUri: String?
)

fun PlayerState.toEntity(): PlayerStateEntity{
    return PlayerStateEntity(
        positionMs = positionMs,
        defaultQueue = defaultQueue,
        currentSongId = currentSong?.id,
        currentSongUri = currentSong?.uri.toString(),
        currentSongTitle = currentSong?.title,
        currentSongArtist = currentSong?.artist,
        currentSongDuration = currentSong?.duration,
        currentSongAlbum = currentSong?.album,
        currentSongGenre = currentSong?.genre,
        currentSongArtUri = currentSong?.art.toString()
    )
}

fun PlayerStateEntity.toDomain(): PlayerState{
    return PlayerState(
        positionMs = positionMs,
        defaultQueue = defaultQueue,
        currentSong = if (currentSongUri == null) null else Song(
            id = currentSongId ?: "",
            title = currentSongTitle,
            artist = currentSongArtist,
            duration = currentSongDuration ?: 0,
            uri = currentSongUri?.toUri() ?: Uri.EMPTY,
            album = currentSongAlbum,
            genre = currentSongGenre,
            art = currentSongArtUri?.toUri()
        )
    )
}