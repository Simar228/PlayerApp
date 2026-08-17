package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.model.Song

interface PlaybackTransitionRepository {

    suspend fun saveInformationEditSong(
        genre: String,
        song: Song,
    )
    suspend fun startPlayback(
        song: Song,
        defaultQueueSongs: List<Song>?,
        queueItemId: Long?
    )

    suspend fun saveTransition(song: Song, queueItemId: Long?)
}
