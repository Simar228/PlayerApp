package com.example.sound.Presentation.editSongInformation.viewModel

import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.FakeEditSongRepository
import com.example.sound.Domain.repository.FakeGenreRepository
import com.example.sound.Domain.repository.FakeImageRepository
import com.example.sound.Domain.repository.FakeSongRepository
import com.example.sound.Domain.useCase.editSong.SaveSongUseCase
import com.example.sound.Domain.useCase.editSong.SetSongUseCase
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditSongViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: EditSongViewModel
    private lateinit var song: Song
    private lateinit var genreRepository: FakeGenreRepository
    private lateinit var editSongRepository: FakeEditSongRepository
    private lateinit var imageRepository: FakeImageRepository
    private lateinit var songRepository: FakeSongRepository
    private lateinit var genres: List<Genre>

    @Before
    fun setUp() {
        song = FakeSong.SONG_0
        genres = listOf(
            Genre(id = 1L, name = "Rock"),
            Genre(id = 2L, name = "Pop"),
        )
        genreRepository = FakeGenreRepository().apply {
            this.genres = this@EditSongViewModelTest.genres
        }
        songRepository = FakeSongRepository()
        editSongRepository = FakeEditSongRepository()
        imageRepository = FakeImageRepository()
        sut = EditSongViewModel(
            song = song,
            setSongUseCase = SetSongUseCase(editSongRepository),
            saveSongUseCase = SaveSongUseCase(
                imageRepository = imageRepository,
                songRepository = songRepository,
                genreRepository = genreRepository,
                editSongRepository = editSongRepository,
            ),
            genreRepository = genreRepository,
        )
    }

    @Test
    fun `init updates genres in uiState`() {
        assertThat(sut.uiState.value.genres).isEqualTo(genres)
    }

    @Test
    fun `setArt marks song as edited and updates art in uiState`() {
        sut.setArt("content://new-artwork")

        assertThat(sut.edited.value).isTrue()
        assertThat(sut.uiState.value.art).isEqualTo("content://new-artwork")
    }

    @Test
    fun `sendEvent with unchanged values does not update state or edited flag`() {
        val initialState = sut.uiState.value

        sut.sendEvent(EditSongEvent.EditSongTitle(song.title.orEmpty()))
        sut.sendEvent(EditSongEvent.EditSongArtist(song.artist.orEmpty()))
        sut.sendEvent(EditSongEvent.EditSongAlbum(song.album.orEmpty()))
        sut.sendEvent(EditSongEvent.EditSongGenre(song.genre.orEmpty()))

        assertThat(sut.uiState.value).isSameInstanceAs(initialState)
        assertThat(sut.edited.value).isFalse()
    }

    @Test
    fun `sendEvent with changed values marks song as edited and updates uiState`() {
        sut.sendEvent(EditSongEvent.EditSongTitle("New title"))
        sut.sendEvent(EditSongEvent.EditSongArtist("New artist"))
        sut.sendEvent(EditSongEvent.EditSongAlbum("New album"))
        sut.sendEvent(EditSongEvent.EditSongGenre("Jazz"))

        assertThat(sut.edited.value).isTrue()
        assertThat(sut.uiState.value).isEqualTo(
            EditSongUiState(
                title = "New title",
                artist = "New artist",
                album = "New album",
                genre = "Jazz",
                art = song.art,
                genres = genres,
            )
        )
    }

    @Test
    fun `setSong invokes SetSongUseCase`() = runTest {
        sut.setArt("content://new-artwork")

        sut.setSong()
        advanceUntilIdle()

        assertThat(editSongRepository.setEditSongCalls).containsExactly(song.id)
        assertThat(sut.edited.value).isFalse()
    }

    @Test
    fun `saveSong does not invoke SaveSongUseCase when song is not edited`() = runTest {
        sut.saveSong()
        advanceUntilIdle()

        assertThat(imageRepository.saveImageCalls).isEmpty()
    }

    @Test
    fun `saveSong invokes SaveSongUseCase when song is edited`() = runTest {
        sut.sendEvent(EditSongEvent.EditSongTitle("New title"))

        sut.saveSong()
        advanceUntilIdle()

        assertThat(editSongRepository.editSongList).hasSize(1)
        assertThat(genreRepository.genres).hasSize(3)
    }
}
