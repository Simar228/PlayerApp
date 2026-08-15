package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Genre

interface GenreRepository {

    suspend fun getGenres(): List<Genre>

    suspend fun addGenre(genre: Genre)

    suspend fun deleteGenre(id: Long)
}