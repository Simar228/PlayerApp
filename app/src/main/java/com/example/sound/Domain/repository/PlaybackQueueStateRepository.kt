package com.example.sound.Domain.repository

import com.example.sound.Domain.model.PlaybackQueueState
import kotlinx.coroutines.flow.Flow

interface PlaybackQueueStateRepository {
    fun observePlaybackQueueState(): Flow<PlaybackQueueState>
}