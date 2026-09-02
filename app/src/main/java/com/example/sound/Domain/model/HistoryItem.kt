package com.example.sound.Domain.model

data class HistoryItem(
    val song: Song,
    val playedAt: Long,
    val position: Int,
)