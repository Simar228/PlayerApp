package com.example.sound.testing

import com.example.sound.Domain.model.Song

fun createTestSong(id: String) = Song(
    id = id,
    title = "Song $id",
    artist = "Artist",
    duration = 1_000L,
    uri = "content://song/$id",
    album = null,
    genre = null,
    art = null,
)