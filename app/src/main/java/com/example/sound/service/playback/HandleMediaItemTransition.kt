package com.example.sound.service.playback

import androidx.media3.common.MediaItem
import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Domain.repository.HistoryQueueRepository
import com.example.sound.Domain.repository.PlayerQueueRepository
import javax.inject.Inject

class HandleMediaItemTransition @Inject constructor(
    private val historyQueueRepository: HistoryQueueRepository,
    private val playerQueueRepository: PlayerQueueRepository,
    private val database: AppDatabase,
) {
    suspend operator fun invoke(mediaItem: MediaItem?) {
        mediaItem?.let {
            database.withTransaction {
                val timeStamp = System.currentTimeMillis()
                historyQueueRepository.addHistoryItem(song = mediaItem.toSong(), timeStamp)
                val queueItemId = mediaItem.queueItemIdOrNull() ?: return@withTransaction
                playerQueueRepository.deleteQueueItemById(queueItemId)
            }
        }
    }
}