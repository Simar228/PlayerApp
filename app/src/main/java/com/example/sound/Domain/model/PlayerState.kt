package com.example.sound.Domain.model

data class PlayerState(
    val defaultQueue: Boolean,
    val currentSong: Song?
)