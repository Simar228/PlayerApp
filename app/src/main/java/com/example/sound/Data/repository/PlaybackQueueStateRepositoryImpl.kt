package com.example.sound.Data.repository

import androidx.room.withTransaction
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Data.local.playerState.PlayerStateDao
import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Domain.model.PlaybackQueueState
import com.example.sound.Domain.repository.PlaybackQueueStateRepository
import com.example.sound.Domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import com.example.sound.Data.local.playerState.toDomain as toPlayerState
import com.example.sound.Data.local.queue.toDomain as toQueueItem

class PlaybackQueueStateRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val playerStateDao: PlayerStateDao,
    private val queueDao: QueueDao,
    private val songRepository: SongRepository,
) : PlaybackQueueStateRepository {

    override fun observePlaybackQueueState(): Flow<PlaybackQueueState> {
        val databaseFlow = database.invalidationTracker
            .createFlow(
                DatabaseTableNames.PLAYER_STATE,
                DatabaseTableNames.QUEUE_ITEMS,
            )
            .conflate()

        return combine(
            databaseFlow,
            songRepository.songs,
        ) { _, songs ->
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

                    defaultQueueSongs = songs
                )
            }
        }.distinctUntilChanged()
    }
}
