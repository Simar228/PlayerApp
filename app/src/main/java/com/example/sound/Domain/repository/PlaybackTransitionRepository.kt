package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song

interface PlaybackTransitionRepository {
    suspend fun startPlayback(song: Song, defaultQueueSongs: List<Song>)
    suspend fun saveTransition(song: Song, queueItemId: Long?)
}