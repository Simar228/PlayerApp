package com.example.sound.Domain.useCase.queue


import com.example.sound.Domain.model.QueueItem
import javax.inject.Inject

class MoveQueueItemUseCase @Inject constructor() {

    operator fun invoke(songQueue: List<QueueItem>, fromIndex: Int, toIndex: Int): List<QueueItem> {

        if (fromIndex !in songQueue.indices || toIndex !in songQueue.indices || songQueue.isEmpty()) {
            return songQueue
        }


        val updatedQueue = songQueue.toMutableList()
        val movedItem = updatedQueue.removeAt(fromIndex)
        updatedQueue.add(toIndex, movedItem)

        return updatedQueue
    }
}