package com.example.sound.Domain.useCase.editSong

import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.model.Song
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
    private lateinit var playbackTransitionRepository: FakePlaybackTransitionRepository
    private lateinit var originalSong: Song
    private lateinit var songRepository: FakeSongRepository
    private lateinit var uiState: MutableStateFlow<EditSongUiState>

    @Before
    fun setUp() {
        songRepository = FakeSongRepository()
        originalSong = FakeSong.SONG_0
        imageRepository = FakeImageRepository().apply {
            savedImagePath = "file://saved-artwork"
        }
        playbackTransitionRepository = FakePlaybackTransitionRepository()
        sut = SaveSongUseCase(
            playbackTransitionRepository = playbackTransitionRepository,
            imageRepository = imageRepository,
            songRepository = songRepository
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

        val expectedSavedSong = FakePlaybackTransitionRepository.SaveInformationEditSongCall(
            newSong = Song(
                id = originalSong.id,
                title = "Edited title",
                artist = "Edited artist",
                duration = originalSong.duration,
                uri = originalSong.uri,
                album = "Edited album",
                genre = "Jazz",
                art = "file://saved-artwork",
            ),
            originalSong = FakeSong.SONG_0.copy(
                genre = "Original Genre",
                art = "Original Art",
                album = "Original Album",
                title = "Original Title",
                artist = "Original Artist",
            ),
        )

        assertThat(imageRepository.saveImageCalls).containsExactly("content://selected-artwork")
        assertThat(playbackTransitionRepository.saveInformationEditSongCalls)
            .containsExactly(
                expectedSavedSong
            )
    }

    @Test
    fun `if original songs isn't in songsRepository - original song is last copy of song`() =
        runTest {
            sut(uiState = uiState, song = originalSong)

            val expectedSavedSong = FakePlaybackTransitionRepository.SaveInformationEditSongCall(
                newSong = Song(
                    id = originalSong.id,
                    title = "Edited title",
                    artist = "Edited artist",
                    duration = originalSong.duration,
                    uri = originalSong.uri,
                    album = "Edited album",
                    genre = "Jazz",
                    art = "file://saved-artwork",
                ),
                originalSong = originalSong,
            )

            assertThat(imageRepository.saveImageCalls).containsExactly("content://selected-artwork")
            assertThat(playbackTransitionRepository.saveInformationEditSongCalls)
                .containsExactly(
                    expectedSavedSong
                )
        }
}
