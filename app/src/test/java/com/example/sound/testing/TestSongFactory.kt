package com.example.sound.testing

import com.example.sound.Domain.model.Song

 fun createTestSong(
    id: String = "1",
    title: String? = "Song $id",
    artist: String? = "Artist",
    duration: Long = 1_000L,
    uri: String = "content://song/$id",
    album: String? = null,
    genre: String? = null,
    art: String? = null,
): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        uri = uri,
        album = album,
        genre = genre,
        art = art,
    )
}