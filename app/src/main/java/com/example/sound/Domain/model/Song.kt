package com.example.sound.Domain.model



data class Song(
    val id: String,
    val title: String?,
    val artist: String?,
    val duration: Long,
    val uri: String,
    val album: String?,
    val genre: String?,
    val art: String? = null
)
