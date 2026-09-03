package com.example.sound.Data.repository

import com.example.sound.Data.local.queue.QueueDao
import com.example.sound.Data.local.queue.toDomain
import com.example.sound.Domain.model.PlaybackQueueState
import com.example.sound.Domain.repository.PlaybackQueueStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlaybackQueueStateRepositoryImpl @Inject constructor(
    private val queueDao: QueueDao,
) : PlaybackQueueStateRepository {

    override fun observePlaybackQueueState(): Flow<PlaybackQueueState> {
        return queueDao.observeQueue()
            .map { entities ->
                PlaybackQueueState(
                    queueItems = entities.map { entity -> entity.toDomain() }
                )
            }
            .distinctUntilChanged()
    }
}
