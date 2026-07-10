package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Song

interface SongRepository {
    fun getSong(): List<Song>
}