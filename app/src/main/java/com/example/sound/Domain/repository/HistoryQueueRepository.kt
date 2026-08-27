package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song

interface HistoryQueueRepository {

    suspend fun addHistoryItem(song: Song)

}