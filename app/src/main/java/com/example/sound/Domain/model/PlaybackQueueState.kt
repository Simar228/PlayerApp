package com.example.sound.Domain.model

data class PlaybackQueueState(
    val playerQueueSongs: List<QueueItem>,
    val historyQueueSongs: List<Song>
)