package com.example.sound.Domain.model

data class PlayerState(
    val positionMs: Long?,
    val defaultQueue: Boolean,

    val currentSong: Song?
)