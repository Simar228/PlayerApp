package com.example.sound.Domain.model

data class PlayerState(
    val currentQueueItemId: Long?,
    val positionMs: Long
)