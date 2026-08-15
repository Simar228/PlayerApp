package com.example.sound.Data.local.playerState

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song


@Entity(tableName = DatabaseTableNames.PLAYER_STATE)
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
        currentSongUri = this.uri,
        currentSongTitle = this.title,
        currentSongArtist = this.artist,
        currentSongDuration = this.duration,
        currentSongAlbum = this.album,
        currentSongGenre = this.genre,
        currentSongArtUri = this.art,
    )
}

fun PlayerStateEntity.toDomain(): PlayerState {
    return PlayerState(
        Song(
            id = currentSongId,
            title = currentSongTitle,
            artist = currentSongArtist,
            duration = currentSongDuration,
            uri = currentSongUri,
            album = currentSongAlbum,
            genre = currentSongGenre,
            art = currentSongArtUri
        )
    )
}