package com.example.sound.Domain.model

data class QueueItem(
    val id: Long,
    val song: Song,
    val position: Int
)