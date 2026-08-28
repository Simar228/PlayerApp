package com.example.sound.Domain.repository

import com.example.sound.Domain.model.Genre

class FakeGenreRepository : GenreRepository {
    var genres: List<Genre> = emptyList()

    override suspend fun getGenres(): List<Genre> = genres
    override suspend fun insertGenre(genre: String) {
        genres += Genre(
            name = genre,
            id = 0
        )
    }
}
