package com.example.sound.Data.repository

import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.defualtQueue.toDomain
import com.example.sound.Data.local.defualtQueue.toEntity
import com.example.sound.Data.local.defualtQueue.toSong
import com.example.sound.Domain.model.DefaultQueueItem
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.DefaultQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class DefaultQueueRepositoryImpl @Inject constructor(
    val defaultQueueDao: DefaultQueueDao
) : DefaultQueueRepository {
    override suspend fun updateDefaultQueue(newDefaultQueue: List<Song>) {
        val songsList = newDefaultQueue.toMutableList()
        val defaultQueueItemList = songsList
            .mapIndexed { index, song ->
                DefaultQueueItem(
                    id = index,
                    song = song
                )
            }
        val newEntityList = defaultQueueItemList.mapIndexed { index, item ->
            item.toEntity(index)
        }
        defaultQueueDao.replaceDefaultQueue(newEntityList)
    }

    override fun observeQueue(): Flow<List<Song>> {
        return defaultQueueDao.observeDefaultQueue()
            .map {
                it.map { entity ->
                    entity.toDomain().toSong()
                }
            }
    }


    override suspend fun getDefaultQueue(): List<Song> {
        return defaultQueueDao.getDefaultQueue().map {
            it.toDomain().toSong()
        }
    }
}