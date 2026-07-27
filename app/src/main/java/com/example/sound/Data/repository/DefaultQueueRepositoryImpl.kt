package com.example.sound.Data.repository

import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.defualtQueue.toDefaultQueueEntity
import com.example.sound.Data.local.defualtQueue.toSong
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.DefaultQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class DefaultQueueRepositoryImpl @Inject constructor(
    val defaultQueueDao: DefaultQueueDao
) : DefaultQueueRepository {
    override suspend fun updateDefaultQueue(newDefaultQueue: List<Song>) {

        val newEntityList = newDefaultQueue.mapIndexed { index, item ->
            item.toDefaultQueueEntity(index)
        }
        defaultQueueDao.replaceDefaultQueue(newEntityList)
    }

    override fun observeQueue(): Flow<List<Song>> {
        return defaultQueueDao.observeDefaultQueue().map {
            it.map { entity ->
                entity.toSong()
            }
        }
    }
}