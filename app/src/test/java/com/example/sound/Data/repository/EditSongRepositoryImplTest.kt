package com.example.sound.Data.repository

import com.example.sound.Data.editSong.FakeEditSongDao
import com.example.sound.Data.local.editSong.EditSongItemEntity
import com.example.sound.Domain.model.Song
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EditSongRepositoryImplTest {

    lateinit var sut: EditSongRepositoryImpl
    lateinit var dao: FakeEditSongDao

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        dao = FakeEditSongDao()
        sut = EditSongRepositoryImpl(dao)
    }

    @Test
    fun `setEditSong returns original song when song exists`() = runTest {
        dao.addEditSong(createEditSongEntity())

        val result = sut.setEditSong(
            "songId_0"
        )

        assertThat(result).isEqualTo(
            Song(
                id = "songId_0",
                title = "original_title",
                artist = "original_artist",
                duration = 10_000,
                uri = "Uri",
                album = "original_album",
                genre = "original_genre",
                art = "original_imagePath"
            )
        )

    }

    @Test
    fun `setEditSong returns null when song does not exist`() =
        runTest {
            val originalSong = sut.setEditSong(
                "songId_0"
            )
            assertThat(originalSong).isNull()
        }

    @Test
    fun `observe editSong emits current history`() = runTest {
        val editSong = createEditSongEntity()
        dao.addEditSong(
            editSong
        )

        val result = sut.observeEditSongs().first()

        assertThat(result)
            .containsExactly(
                Song(
                    id = "songId_0",
                    title = "edit_title",
                    artist = "edit_artist",
                    duration = 10_000,
                    uri = "Uri",
                    album = "edit_album",
                    genre = "edit_genre",
                    art = "edit_imagePath"
                )
            )
            .inOrder()
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