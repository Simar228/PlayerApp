package com.example.sound.Domain.useCase.queue

import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerQueueRepository
import javax.inject.Inject

class ChooseNextSongUseCase @Inject constructor(
    private val playerQueueRepository: PlayerQueueRepository
) {
    suspend operator fun invoke(song: Song){
        val queueItem = QueueItem(
            id = 0,
            song = song,
            position = 0
        )
        playerQueueRepository.insertQueueItem(queueItem)
    }
}