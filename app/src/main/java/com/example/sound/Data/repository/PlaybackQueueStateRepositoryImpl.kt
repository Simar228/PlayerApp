package com.example.sound.Data.repository

import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Domain.model.PlaybackQueueState
import com.example.sound.Domain.repository.HistoryQueueRepository
import com.example.sound.Domain.repository.PlaybackQueueStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlaybackQueueStateRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val queueDao: QueueDao,
    private val historyQueueRepository: HistoryQueueRepository,
) : PlaybackQueueStateRepository {

    override fun observePlaybackQueueState(): Flow<PlaybackQueueState> {
        val databaseFlow = database.invalidationTracker
            .createFlow(
                DatabaseTableNames.QUEUE_ITEMS,
            )
            .conflate()

        return combine(
            databaseFlow,


            ) { _ ->
            database.withTransaction {
                PlaybackQueueState(
                    historyQueueSongs = historyQueueRepository.observeHistoryQueue().first()
                        .map { it.song },
                    playerQueueSongs = queueDao
                        .getQueue()
                        .map { entity ->
                            entity.song
                        },
                )
            }
        }.distinctUntilChanged()
    }
}
