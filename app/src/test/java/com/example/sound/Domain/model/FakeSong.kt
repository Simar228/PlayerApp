package com.example.sound.Domain.model

object FakeSong {

    val SONG_1 = Song(
        id = "song_id_1",
        title = "Bohemian Rhapsody",
        artist = "Queen",
        duration = 354000L,
        uri = "content://media/external/audio/media/1",
        album = "A Night at the Opera",
        genre = "Rock",
        art = "https://example.com"
    )

    val SONG_2 = Song(
        id = "song_id_2",
        title = "Blinding Lights",
        artist = "The Weeknd",
        duration = 200000L,
        uri = "content://media/external/audio/media/2",
        album = "After Hours",
        genre = "Pop",
        art = "https://example.com"
    )

    val SONG_3 = Song(
        id = "song_id_3",
        title = "Shape of You",
        artist = "Ed Sheeran",
        duration = 233000L, // 3:53
        uri = "content://media/external/audio/media/3",
        album = "÷ (Divide)",
        genre = "Pop"
    )

    val SONG_4 = Song(
        id = "song_id_4",
        title = null,
        artist = "Unknown Artist",
        duration = 180000L,
        uri = "content://media/external/audio/media/4",
        album = null,
        genre = "Lo-Fi"
    )

    val SONG_5 = Song(
        id = "song_id_5",
        title = "Lose Yourself",
        artist = "Eminem",
        duration = 326000L, // 5:26
        uri = "content://media/external/audio/media/5",
        album = "8 Mile",
        genre = "Hip-Hop",
        art = null
    )
}