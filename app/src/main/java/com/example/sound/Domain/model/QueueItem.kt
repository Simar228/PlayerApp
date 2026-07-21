package com.example.sound.Domain.model

data class QueueItem(
    val id: Long,
    val song: Song,
    val position: Int
)

fun QueueItem.toSong(): Song{
    return Song(
        id = song.id,
        title = song.title,
        artist = song.artist,
        duration = song.duration,
        uri = song.uri,
        album = song.album,
        genre = song.genre,
        art = song.art
    )
}