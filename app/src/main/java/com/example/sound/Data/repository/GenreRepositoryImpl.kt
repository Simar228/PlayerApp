package com.example.sound.Data.repository

import com.example.sound.Data.local.Genre.GenreDao
import com.example.sound.Data.local.Genre.toEntity
import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.repository.GenreRepository
import javax.inject.Inject


class GenreRepositoryImpl @Inject constructor(
    private val genreDao: GenreDao
) : GenreRepository {
    override suspend fun getGenres(): List<Genre> {

        val genreList = genreDao.getAllGenres().map { entity ->
            Genre(
                name = entity.name,
                id = entity.id
            )
        }
        return genreList
    }

    override suspend fun addGenre(genre: Genre) {
        val genreEntity = genre.toEntity()
        genreDao.insertGenre(genreEntity)
    }

    override suspend fun deleteGenre(id: Long) {
        TODO("Not yet implemented")
    }

}