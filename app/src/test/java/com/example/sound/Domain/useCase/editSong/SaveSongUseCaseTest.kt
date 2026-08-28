package com.example.sound.Domain.useCase.editSong

import com.example.sound.Data.local.editSong.EditSongItemEntity
import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.FakeEditSongRepository
import com.example.sound.Domain.repository.FakeGenreRepository
import com.example.sound.Domain.repository.FakeImageRepository
import com.example.sound.Domain.repository.FakePlaybackTransitionRepository
import com.example.sound.Domain.repository.FakeSongRepository
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongUiState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveSongUseCaseTest {


    private lateinit var sut: SaveSongUseCase
    private lateinit var imageRepository: FakeImageRepository
    private lateinit var originalSong: Song
    private lateinit var songRepository: FakeSongRepository
    private lateinit var uiState: MutableStateFlow<EditSongUiState>
    private lateinit var fakeGenreRepository: FakeGenreRepository
    private lateinit var fakeEditSongRepository: FakeEditSongRepository

    @Before
    fun setUp() {
        fakeEditSongRepository = FakeEditSongRepository()
        fakeGenreRepository = FakeGenreRepository()
        songRepository = FakeSongRepository()
        originalSong = FakeSong.SONG_0.copy(genre = "jazz")
        imageRepository = FakeImageRepository().apply {
            savedImagePath = "file://saved-artwork"
        }
        sut = SaveSongUseCase(
            imageRepository = imageRepository,
            songRepository = songRepository,
            genreRepository = fakeGenreRepository,
            editSongRepository = fakeEditSongRepository,
        )
        uiState = MutableStateFlow(
            EditSongUiState(
                title = "Edited title",
                artist = "Edited artist",
                album = "Edited album",
                genre = "Jazz",
                art = "content://selected-artwork",
            )
        )
    }

    @Test
    fun `invoke saves artwork, edited song and original song`() = runTest {
        songRepository.setOriginalSongs(
            listOf(
                FakeSong.SONG_0.copy(
                    genre = "Original Genre",
                    art = "Original Art",
                    album = "Original Album",
                    title = "Original Title",
                    artist = "Original Artist",
                )
            )
        )

        sut(uiState = uiState, song = originalSong)

        assertThat(imageRepository.saveImageCalls).containsExactly("content://selected-artwork")
        assertThat(fakeEditSongRepository.editSongList).containsExactly(
            EditSongItemEntity(
                id = 0,
                songId = originalSong.id,
                editSongTitle = "Edited title",
                editSongArtist = "Edited artist",
                songDuration = originalSong.duration,
                songUri = originalSong.uri,
                editSongAlbum = "Edited album",
                editSongGenre = "Jazz",
                editSongImagePath = "file://saved-artwork",
                originalSongTitle = "Original Title",
                originalSongArtist = "Original Artist",
                originalSongAlbum = "Original Album",
                originalSongGenre = "Original Genre",
                originalSongImagePath = "Original Art",
            )
        )
        assertThat(fakeGenreRepository.genres).containsExactly(
            Genre(
                name = "Jazz", id = 0
            )
        )
    }

    @Test
    fun `if original songs isn't in songsRepository - original song is last copy of song`() =
        runTest {
            sut(uiState = uiState, song = originalSong)

            assertThat(imageRepository.saveImageCalls).containsExactly("content://selected-artwork")
            assertThat(fakeEditSongRepository.editSongList).containsExactly(
                EditSongItemEntity(
                    id = 0,
                    songId = originalSong.id,
                    editSongTitle = "Edited title",
                    editSongArtist = "Edited artist",
                    songDuration = originalSong.duration,
                    songUri = originalSong.uri,
                    editSongAlbum = "Edited album",
                    editSongGenre = "Jazz",
                    editSongImagePath = "file://saved-artwork",
                    originalSongTitle = originalSong.title,
                    originalSongArtist = originalSong.artist,
                    originalSongAlbum = originalSong.album,
                    originalSongGenre = originalSong.genre,
                    originalSongImagePath = originalSong.art
                )
            )
            assertThat(fakeGenreRepository.genres).containsExactly(
                Genre(
                    name = "Jazz", id = 0
                )
            )
        }
}
