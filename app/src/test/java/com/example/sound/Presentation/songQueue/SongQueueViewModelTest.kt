package com.example.sound.Presentation.songQueue

import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.Presentation.activity.MainDispatcherRule
import com.example.sound.testing.createTestSong
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SongQueueViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mock<PlayerQueueRepository>()

    @Test
    fun `moveQueueItem moves item to requested index`() {
        // Given
        val itemA = createQueueItem(id = 1L, songId = "A", position = 0)
        val itemB = createQueueItem(id = 2L, songId = "B", position = 1)
        val itemC = createQueueItem(id = 3L, songId = "C", position = 2)
        whenever(repository.observeQueue()).thenReturn(
            flowOf(listOf(itemA, itemB, itemC))
        )
        val viewModel = SongQueueViewModel(repository)

        // When
        viewModel.moveQueueItem(fromIndex = 0, toIndex = 2)

        // Then
        assertEquals(
            listOf(itemB, itemC, itemA),
            viewModel.songQueue.value,
        )
    }

    @Test
    fun `moveQueueItem keeps queue unchanged for invalid indexes`() {
        // Given
        val itemA = createQueueItem(id = 1L, songId = "A", position = 0)
        val itemB = createQueueItem(id = 2L, songId = "B", position = 1)
        val initialQueue = listOf(itemA, itemB)
        whenever(repository.observeQueue()).thenReturn(flowOf(initialQueue))
        val viewModel = SongQueueViewModel(repository)

        // When
        viewModel.moveQueueItem(fromIndex = -1, toIndex = 0)
        viewModel.moveQueueItem(fromIndex = 0, toIndex = initialQueue.size)

        // Then
        assertEquals(initialQueue, viewModel.songQueue.value)
    }

    private fun createQueueItem(
        id: Long,
        songId: String,
        position: Int,
    ) = QueueItem(
        id = id,
        song = createTestSong(id = songId),
        position = position,
    )
}
