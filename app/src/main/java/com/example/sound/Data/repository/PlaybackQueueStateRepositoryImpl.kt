package com.example.sound.Data.repository

import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.defualtQueue.toSong
import com.example.sound.Data.local.playerstate.PlayerStateDao
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Domain.model.PlaybackQueueState
import com.example.sound.Domain.repository.PlaybackQueueStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.example.sound.Data.local.playerstate.toDomain as toPlayerState
import com.example.sound.Data.local.queue.toDomain as toQueueItem

class PlaybackQueueStateRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val playerStateDao: PlayerStateDao,
    private val queueDao: QueueDao,
    private val defaultQueueDao: DefaultQueueDao,
) : PlaybackQueueStateRepository {

    override fun observePlaybackQueueState(): Flow<PlaybackQueueState> {
        return database.invalidationTracker
            .createFlow(
                "player_state",
                "queue_items",
                "defaultQueue_items",
            )
            .map {
                database.withTransaction {
                    PlaybackQueueState(
                        currentSong = playerStateDao
                            .getPlayerState()
                            ?.toPlayerState()
                            ?.currentSong,

                        queueItems = queueDao
                            .getQueue()
                            .map { entity ->
                                entity.toQueueItem()
                            },

                        defaultQueueSongs = defaultQueueDao
                            .getDefaultQueue()
                            .map { entity ->
                                entity.toSong()
                            },
                    )
                }
            }
            .distinctUntilChanged()
    }
}