package com.example.sound.testing

import com.example.sound.Data.local.queue.QueueItemEntity
import com.example.sound.Domain.model.Song

fun createTestQueueItem(
    id: Long,
    song: Song,
    position: Int,
) = QueueItemEntity(
    id = id,
    songId = song.id,
    songUri = song.uri,
    position = position,
    title = song.title,
    artist = song.artist,
    duration = song.duration,
    album = song.album,
    genre = song.genre,
    artUri = song.art,
)