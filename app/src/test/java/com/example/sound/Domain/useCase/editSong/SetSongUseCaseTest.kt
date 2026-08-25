package com.example.sound.Domain.useCase.editSong

import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.FakeEditSongRepository
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongUiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SetSongUseCaseTest {

    private lateinit var sut: SetSongUseCase
    private lateinit var repository: FakeEditSongRepository
    private lateinit var song: Song
    private lateinit var uiState: MutableStateFlow<EditSongUiState>
    private lateinit var edited: MutableStateFlow<Boolean>

    @Before
    fun setUp() {
        edited = MutableStateFlow(true)
        repository = FakeEditSongRepository()
        sut = SetSongUseCase(repository)
        song = FakeSong.SONG_0
        uiState = MutableStateFlow(
            EditSongUiState(
                title = "Edited title",
                artist = "Edited artist",
                album = "Edited album",
                genre = "Jazz",
                art = "content://edited-artwork",
                genres = listOf(Genre(id = 1L, name = "Rock")),
            )
        )
    }

    @Test
    fun `invoke restores song metadata returned by repository`() = runTest {
        repository.setEditSongResult = FakeSong.SONG_1

        sut(uiState = uiState, song = song, edited)

        assertThat(edited.value).isFalse()
        assertThat(repository.setEditSongCalls).containsExactly(song.id)
        assertThat(uiState.value).isEqualTo(
            EditSongUiState(
                title = FakeSong.SONG_1.title.orEmpty(),
                artist = FakeSong.SONG_1.artist.orEmpty(),
                album = FakeSong.SONG_1.album.orEmpty(),
                genre = FakeSong.SONG_1.genre.orEmpty(),
                art = FakeSong.SONG_1.art,
                genres = listOf(Genre(id = 1L, name = "Rock")),
            )
        )
    }

    @Test
    fun `invoke restores standard song when repository has no saved song`() = runTest {
        sut(uiState = uiState, song = song, edited)

        assertThat(edited.value).isFalse()
        assertThat(repository.setEditSongCalls).containsExactly(song.id)
        assertThat(uiState.value).isEqualTo(
            EditSongUiState(
                title = song.title.orEmpty(),
                artist = song.artist.orEmpty(),
                album = song.album.orEmpty(),
                genre = song.genre.orEmpty(),
                art = song.art,
                genres = listOf(Genre(id = 1L, name = "Rock")),
            )
        )
    }
}
