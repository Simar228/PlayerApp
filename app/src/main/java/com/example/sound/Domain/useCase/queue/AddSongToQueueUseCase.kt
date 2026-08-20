package com.example.sound.Domain.useCase.queue

import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerQueueRepository
import jakarta.inject.Inject

class AddSongAtTheEndQueueUseCase @Inject constructor(
    private val playerQueueRepository: PlayerQueueRepository
) {
    suspend operator fun invoke(song: Song) {
        playerQueueRepository.insertQueueItemAtTheEnd(song)
    }
}