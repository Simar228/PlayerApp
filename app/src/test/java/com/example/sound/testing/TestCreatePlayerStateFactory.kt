package com.example.sound.testing

import com.example.sound.Data.local.playerState.PlayerStateEntity
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song

fun createTestPlayerStateEntity(currentSong: Song): PlayerStateEntity {
    return PlayerStateEntity(
        currentSongId = currentSong.id,
        currentSongUri = currentSong.uri,
        currentSongTitle = currentSong.title,
        currentSongArtist = currentSong.artist,
        currentSongDuration = currentSong.duration,
        currentSongAlbum = currentSong.album,
        currentSongGenre = currentSong.genre,
        currentSongArtUri = currentSong.art
    )
}