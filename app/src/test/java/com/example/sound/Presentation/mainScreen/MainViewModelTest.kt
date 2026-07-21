package com.example.sound.Presentation.mainScreen

import android.net.Uri
import com.example.sound.Domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class MainViewModelTest {

    @Test
    fun `first title sort orders songs`() {
        val viewModel = MainViewModel()
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
            uri = mock(Uri::class.java),
            album = null,
            genre = null,
            art = null
        )
    }
}