package com.example.sound.Domain.useCase.queue

import com.example.sound.Domain.model.FakeQueueItem
import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.repository.FakePlayerQueueRepository
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddSongAtTheEndQueueUseCaseTest {

    lateinit var sut: AddSongAtTheEndQueueUseCase
    lateinit var repository: FakePlayerQueueRepository

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        repository = FakePlayerQueueRepository()
        sut = AddSongAtTheEndQueueUseCase(repository)
        repository.fakeSetQueueItems(
            listOf(
                FakeQueueItem.create(),
                FakeQueueItem.create(id = 1, song = FakeSong.SONG_1, position = 1),
            )
        )
    }

    @Test
    fun `if song added it must be at the end`() = runTest {
        val song = FakeSong.SONG_2

        sut(song)

        val currentList = repository.getFakeQueueItems()
        val expectedList = listOf(
            FakeQueueItem.create(),
            FakeQueueItem.create(id = 1, song = FakeSong.SONG_1, position = 1),
            FakeQueueItem.create(id = 2, song = FakeSong.SONG_2, position = 2)
        )

        assertThat(currentList).containsExactlyElementsIn(expectedList).inOrder()
    }


}