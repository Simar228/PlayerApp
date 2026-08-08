package com.example.sound.Domain.model

data class PlaybackQueueState(
    val currentSong: Song?,
    val queueItems: List<QueueItem>,
    val defaultQueueSongs: List<Song>,
)