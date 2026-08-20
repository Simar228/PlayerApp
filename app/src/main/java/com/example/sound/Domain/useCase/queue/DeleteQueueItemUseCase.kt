package com.example.sound.Domain.useCase.queue


import com.example.sound.Domain.repository.PlayerQueueRepository
import javax.inject.Inject

class DeleteQueueItemUseCase @Inject constructor(
    private val playerQueueRepository: PlayerQueueRepository
) {
    suspend operator fun invoke(queueItemId: Long) {
        playerQueueRepository.deleteQueueItemById(queueItemId)
    }
}