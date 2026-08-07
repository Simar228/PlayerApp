package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song

interface PlaybackTransitionRepository {
    suspend fun saveTransition(song: Song)
}