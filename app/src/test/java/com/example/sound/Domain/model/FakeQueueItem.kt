package com.example.sound.Domain.model

object FakeQueueItem {
    fun create(song: Song, id: Long = 0, position: Int = 0): QueueItem{
        return QueueItem(
            id = id,
            song = song,
            position = position
        )
    }
}