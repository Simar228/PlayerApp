package com.example.sound.Domain.repository

import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow


interface PlayerQueueRepository {

    suspend fun setCurrentSong(song: Song)
    suspend fun clearQueue()
    suspend fun insertSong(song: Song)

    suspend fun insertSongByIndex(song: Song, position: Int)
    fun observeQueue(): Flow<List<Song>>

    suspend fun saveQueue(queue: List<QueueItem>)

    suspend fun getQueue(): List<QueueItem>

}