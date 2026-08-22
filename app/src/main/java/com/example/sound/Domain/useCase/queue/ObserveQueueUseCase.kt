package com.example.sound.Domain.useCase.queue

import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.repository.PlayerQueueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveQueueUseCase @Inject constructor(
    private val playerQueueRepository: PlayerQueueRepository,
) {
    operator fun invoke(): Flow<List<QueueItem>> {
        return playerQueueRepository.observeQueue()
    }
}