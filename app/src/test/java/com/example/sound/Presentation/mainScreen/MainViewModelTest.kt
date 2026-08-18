package com.example.sound.Presentation.mainScreen


import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.activity.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

class MainViewModelTest {


    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `first title sort orders songs`() {
        val repository = mock<SongRepository>()
        val viewModel = MainViewModel(repository)
        val songs = listOf(
            createSong(id = "1", title = "Beta"),
            createSong(id = "2", title = "Alpha"),
            createSong(id = "3", title = "Gamma")
        )

        val expectedTitlesDescending = listOf(
            "Gamma",
            "Beta",
            "Alpha"
        )

        val expectedTitlesAscending = listOf(
            "Alpha",
            "Beta",
            "Gamma"
        )

        viewModel.setQueueSong(songs)


        viewModel.sortQueueSong(
            MainSortScreenEvents.SortByTitle(isUp = true)
        )


        val actualTitlesDescending =
            viewModel.songsQueue.value.map { it.title }

        assertEquals(expectedTitlesDescending , actualTitlesDescending)

        viewModel.sortQueueSong(
            MainSortScreenEvents.SortByTitle(isUp = false)
        )

        val actualTitlesAscending =
            viewModel.songsQueue.value.map { it.title }

        assertEquals(expectedTitlesAscending, actualTitlesAscending)

    }

    private fun createSong(
        id: String,
        title: String?
    ): Song {
        return Song(
            id = id,
            title = title,
            artist = null,
            duration = 0L,
            uri = "content://song/$id",
            album = null,
            genre = null,
            art = null
        )
    }
}