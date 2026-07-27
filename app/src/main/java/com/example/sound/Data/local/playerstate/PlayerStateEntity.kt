package com.example.sound.Data.local.playerstate

import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song


@Entity(tableName = "player_state")
data class PlayerStateEntity(
    @PrimaryKey
    val id: Int = 0,

    val currentSongId: String,
    val currentSongUri: String,
    val currentSongTitle: String?,
    val currentSongArtist: String?,
    val currentSongDuration: Long,
    val currentSongAlbum: String?,
    val currentSongGenre: String?,
    val currentSongArtUri: String?
)

fun Song.toPlayerStateEntity(): PlayerStateEntity {
    return PlayerStateEntity(
        currentSongId = this.id,
        currentSongUri = this.uri.toString(),
        currentSongTitle = this.title,
        currentSongArtist = this.artist,
        currentSongDuration = this.duration,
        currentSongAlbum = this.album,
        currentSongGenre = this.genre,
        currentSongArtUri = this.art?.toString(),
    )
}

fun PlayerStateEntity.toDomain(): PlayerState {
    return PlayerState(
        Song(
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