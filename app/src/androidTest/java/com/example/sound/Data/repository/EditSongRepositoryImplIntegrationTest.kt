package com.example.sound.Data.repository

import app.cash.turbine.test
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.editSong.EditSongDao
import com.example.sound.Data.local.editSong.EditSongItemEntity
import com.example.sound.Data.local.editSong.toOriginalSong
import com.example.sound.Domain.model.FakeSong
import com.example.sound.utill.InMemoryDatabaseRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EditSongRepositoryImplIntegrationTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule(AppDatabase::class.java)


    private lateinit var sut: EditSongRepositoryImpl
    private lateinit var dao: EditSongDao

    @Before
    fun setUp() {
        val database = dbRule.database
        dao = database.editSongDao()
        sut = EditSongRepositoryImpl(dao)
    }

    @Test
    fun observeEditedSongs_emitsUpdatedList() = runTest {
        sut.observeEditSongs().test {
            assertThat(awaitItem()).isEmpty()

            sut.insertEditSong(
                newSong = FakeSong.SONG_0.copy(artist = "New_artist"),
                oldSong = FakeSong.SONG_0
            )

            assertThat(awaitItem().first().id)
                .isEqualTo("song_id_0")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setSong_deleteEditSongFromDataBaseById_andReturnSong() = runTest {
        dao.addEditSong(createEditSongEntity())

        val result = sut.setEditSong("songId_0")
        val currentList = dao.observeEditSong().first()

        assertThat(result).isEqualTo(createEditSongEntity().toOriginalSong())
        assertThat(currentList).isEmpty()
    }

    private fun createEditSongEntity() =
        EditSongItemEntity(
            id = 0,
            songId = "songId_0",
            editSongTitle = "edit_title",
            editSongArtist = "edit_artist",
            songDuration = 10_000,
            songUri = "Uri",
            editSongAlbum = "edit_album",
            editSongGenre = "edit_genre",
            editSongImagePath = "edit_imagePath",
            originalSongTitle = "original_title",
            originalSongArtist = "original_artist",
            originalSongAlbum = "original_album",
            originalSongGenre = "original_genre",
            originalSongImagePath = "original_imagePath"
        )

}