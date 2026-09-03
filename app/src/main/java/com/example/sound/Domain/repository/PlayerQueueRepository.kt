package com.example.sound.Domain.repository


import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow


interface PlayerQueueRepository {

    suspend fun moveQueueItemToCurrent(queueItemId: Long)
    suspend fun setCurrentSong(currentSong: Song, songs: List<Song>)
    suspend fun deleteQueueItemById(queueItemId: Long)

    suspend fun clearQueue()

    fun observeQueue(): Flow<List<QueueItem>>

    suspend fun saveQueueOrder(queueItemsIds: List<Long>)

    suspend fun insertSongAtTheStart(song: Song)
    suspend fun insertSongAtTheEnd(song: Song)
}
