package com.example.sound.Presentation.songQueue


import com.example.sound.Domain.model.FakeQueueItem
import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.repository.FakePlayerQueueRepository
import com.example.sound.Domain.useCase.queue.MoveQueueItemUseCase
import com.example.sound.Domain.useCase.queue.SaveQueueOrderUseCase
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongQueueViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakePlayerQueueRepository
    private lateinit var moveQueueItemUseCase: MoveQueueItemUseCase
    private lateinit var saveQueueOrderUseCase: SaveQueueOrderUseCase
    private lateinit var sut: SongQueueViewModel

    @Before
    fun setUp() {
        fakeRepository = FakePlayerQueueRepository()
        moveQueueItemUseCase = MoveQueueItemUseCase()
        saveQueueOrderUseCase = SaveQueueOrderUseCase(fakeRepository)

        sut = SongQueueViewModel(
            playerQueueRepository = fakeRepository,
            saveQueueOrderUseCase = saveQueueOrderUseCase,
            moveQueueItemUseCase = moveQueueItemUseCase
        )
    }

    @Test
    fun `init should observe queue from repository and update stateFlow`() = runTest {
        val initialItems = listOf(FakeQueueItem.create(id = 10L, song = FakeSong.SONG_0))
        fakeRepository.fakeSetQueueItems(initialItems)


        advanceUntilIdle()

        val currentUiState = sut.songQueue.value
        assertThat(currentUiState).isEqualTo(initialItems)
    }

    @Test
    fun `addSongToQueue should delegate to repository and append song at the end`() = runTest {
        sut.addSongToQueue(FakeSong.SONG_1)
        advanceUntilIdle()

        val currentQueue = sut.songQueue.value
        assertThat(currentQueue).hasSize(1)
        assertThat(currentQueue.first().song).isEqualTo(FakeSong.SONG_1)
    }

    @Test
    fun `chooseNextSong should insert song at the start of the queue`() = runTest {
        fakeRepository.fakeSetQueueItems(listOf(FakeQueueItem.create(id = 1L, song = FakeSong.SONG_0, position = 0)))
        advanceUntilIdle()


        sut.chooseNextSong(FakeSong.SONG_2)
        advanceUntilIdle()

        val currentQueue = sut.songQueue.value
        assertThat(currentQueue).hasSize(2)
        assertThat(currentQueue.first().song).isEqualTo(FakeSong.SONG_2) // Должна встать первой
    }

    @Test
    fun `moveQueueItem should update local UI state immediately`() = runTest {
        val originalList = listOf(
            FakeQueueItem.create(id = 0L, song = FakeSong.SONG_0, position = 0),
            FakeQueueItem.create(id = 1L, song = FakeSong.SONG_1, position = 1)
        )
        fakeRepository.fakeSetQueueItems(originalList)
        advanceUntilIdle()

        sut.moveQueueItem(fromIndex = 0, toIndex = 1)

        val updatedQueue = sut.songQueue.value
        assertThat(updatedQueue[0].song).isEqualTo(FakeSong.SONG_1)
        assertThat(updatedQueue[1].song).isEqualTo(FakeSong.SONG_0)
    }

    @Test
    fun `saveQueueOrder should call saveQueueOrderUseCase with current queue layout`() = runTest {
        val currentQueue = listOf(
            FakeQueueItem.create(id = 5L, position = 0),
            FakeQueueItem.create(id = 9L, position = 1)
        )
        fakeRepository.fakeSetQueueItems(currentQueue)
        advanceUntilIdle()

        sut.saveQueueOrder()
        advanceUntilIdle()

        val savedIds = fakeRepository.getQueueOfIds()
        assertThat(savedIds).containsExactly(5L, 9L).inOrder()
    }

    @Test
    fun `deleteQueueItem should remove specified item from queue`() = runTest {
        val items = listOf(
            FakeQueueItem.create(id = 100L, position = 0),
            FakeQueueItem.create(id = 200L, position = 1)
        )
        fakeRepository.fakeSetQueueItems(items)
        advanceUntilIdle()

        sut.deleteQueueItem(queueItemId = 100L)
        advanceUntilIdle()

        val currentQueue = sut.songQueue.value
        assertThat(currentQueue).hasSize(1)
        assertThat(currentQueue.first().id).isEqualTo(200L)
    }

    @Test
    fun `clearSongQueue should make ui state flow empty`() = runTest {
        fakeRepository.fakeSetQueueItems(listOf(FakeQueueItem.create()))
        advanceUntilIdle()

        sut.clearSongQueue()
        advanceUntilIdle()
        assertThat(sut.songQueue.value).isEmpty()
    }
}
