package com.example.sound.Domain.model

data class PlaybackQueueState(
    val playerQueueSongs: List<Song>,
    val historyQueueSongs: List<Song>
)