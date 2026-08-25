package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Genre

class FakeGenreRepository : GenreRepository {
    var genres: List<Genre> = emptyList()

    override suspend fun getGenres(): List<Genre> = genres
}
