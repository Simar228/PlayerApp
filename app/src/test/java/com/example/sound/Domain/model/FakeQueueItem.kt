package com.example.sound.Domain.model

object FakeQueueItem {
    fun create(
        id: Long = 0L,
        song: Song = FakeSong.SONG_0,
        position: Int = 0
    ) = QueueItem(id = id, song = song, position = position)
}