package com.example.sound.Data.repository

import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.defualtQueue.toSong
import com.example.sound.Data.local.playerstate.PlayerStateDao
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Domain.model.PlaybackQueueState
import com.example.sound.Domain.repository.PlaybackQueueStateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
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

    @OptIn(FlowPreview::class)
    override fun observePlaybackQueueState(): Flow<PlaybackQueueState> {
        return database.invalidationTracker
            .createFlow(
                DatabaseTableNames.PLAYER_STATE,
                DatabaseTableNames.QUEUE_ITEMS,
                DatabaseTableNames.DEFAULT_QUEUE_ITEMS,
            )
            .conflate()
            .debounce(MEDIA_STORE_CHANGES_DEBOUNCE_MS)
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
            .flowOn(Dispatchers.IO)
            .distinctUntilChanged()
    }
}
private const val MEDIA_STORE_CHANGES_DEBOUNCE_MS = 200L
