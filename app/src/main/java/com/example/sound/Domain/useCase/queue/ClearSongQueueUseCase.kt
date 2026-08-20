package com.example.sound.Domain.useCase.queue

import com.example.sound.Domain.repository.PlayerQueueRepository
import jakarta.inject.Inject

class ClearSongQueueUseCase @Inject constructor(
    private val playerQueueRepository: PlayerQueueRepository
) {
    suspend operator fun invoke() {
        playerQueueRepository.clearQueue()
    }
}