package com.example.sound.Data.repository

import com.example.sound.Data.local.defualtQueue.DefaultQueueDao
import com.example.sound.Data.local.defualtQueue.toDefaultQueueEntity
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.DefaultQueueRepository
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
}