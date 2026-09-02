package com.example.sound.Data.repository

import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.genre.GenreDao
import com.example.sound.Data.local.genre.GenreEntity
import com.example.sound.utill.InMemoryDatabaseRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GenreRepositoryImplIntegrationTest {

    private lateinit var sut: GenreRepositoryImpl
    private lateinit var dao: GenreDao

    @get:Rule
    val dbRule = InMemoryDatabaseRule(AppDatabase::class.java)

    @Before
    fun setUp() {
        val database = dbRule.database
        dao = database.genreDao()
        sut = GenreRepositoryImpl(dao)
    }

    @Test
    fun getGenresReturn_currentGenresList() = runTest {
        dao.insertGenre(
            GenreEntity(
                name = "test",
            )
        )
        val result = sut.getGenres()

        assertThat(result.first().name).isEqualTo("test")

    }

    @Test
    fun insertGenreUpdates_currentGenresList() = runTest {
        sut.insertGenre("test")

        val result = dao.getAllGenres()

        assertThat(result.first().name).isEqualTo("test")
    }
}