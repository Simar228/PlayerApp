package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.model.Song

interface PlaybackTransitionRepository {

    suspend fun updateCurrentSongIfMatches(songs: List<Song>)

    suspend fun saveInformationEditSong(
        genre: String,
        newSong: Song,
        oldSong: Song,
    )
    suspend fun startPlayback(
        song: Song,
        defaultQueueSongs: List<Song>?,
        queueItemId: Long?
    )

    suspend fun saveTransition(song: Song, queueItemId: Long?)
}
