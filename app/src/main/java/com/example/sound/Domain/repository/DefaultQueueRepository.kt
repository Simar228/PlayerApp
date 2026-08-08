package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song

interface DefaultQueueRepository {
    suspend fun updateDefaultQueue(newDefaultQueue: List<Song>)
}