package com.example.sound.Domain.useCase.queue

import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.repository.PlayerQueueRepository
import javax.inject.Inject

class SaveQueueOrderUseCase @Inject constructor(
    private val playerQueueRepository: PlayerQueueRepository
) {

    suspend operator fun invoke(queueItems: List<QueueItem>) {

        val queueItemIds = queueItems.map { it.id }

        playerQueueRepository.saveQueueOrder(queueItemIds)
    }
}