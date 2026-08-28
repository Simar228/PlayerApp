package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song
import kotlinx.coroutines.awaitCancellation

class FakePlaybackTransitionRepository : PlaybackTransitionRepository {
    val startPlaybackCalls = mutableListOf<StartPlaybackCall>()
    val saveInformationEditSongCalls = mutableListOf<SaveInformationEditSongCall>()
    val cancelledPlaybackSongs = mutableListOf<Song>()
    var songToSuspend: Song? = null

    override suspend fun startPlayback(
        song: Song,
        defaultQueueSongs: List<Song>?,
        queueItemId: Long?
    ) {
        startPlaybackCalls += StartPlaybackCall(song, defaultQueueSongs, queueItemId)
        if (song == songToSuspend) {
            try {
                awaitCancellation()
            } finally {
                cancelledPlaybackSongs += song
            }
        }
    }

    override suspend fun updateCurrentSongIfMatches(songs: List<Song>) = Unit

    override suspend fun saveTransition(song: Song, queueItemId: Long?) = Unit


    data class StartPlaybackCall(
        val song: Song,
        val defaultQueueSongs: List<Song>?,
        val queueItemId: Long?
    )

    data class SaveInformationEditSongCall(
        val newSong: Song,
        val originalSong: Song,
    )
}
