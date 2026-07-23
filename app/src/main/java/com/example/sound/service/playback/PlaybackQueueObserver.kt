package com.example.sound.service.playback

import com.example.sound.Domain.repository.DefaultQueueRepository
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.Domain.repository.PlayerStateRepository
import com.example.sound.service.PlaybackQueueState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

class PlaybackQueueObserver @Inject constructor(
    private val playerStateRepository: PlayerStateRepository,
    private val playerQueueRepository: PlayerQueueRepository,
    private val defaultQueueRepository: DefaultQueueRepository,
){
    fun observe(): Flow<PlaybackQueueState> {
        return combine(
            playerStateRepository.observePlayerState(),
            playerQueueRepository.observeQueue(),
            defaultQueueRepository.observeQueue(),
        ) { currentSong, queueSongs, defaultQueueSongs ->
            PlaybackQueueState(
                currentSong = currentSong,
                queueSongs = queueSongs,
                defaultQueueSongs = defaultQueueSongs,
            )
        }.distinctUntilChanged()
    }
}