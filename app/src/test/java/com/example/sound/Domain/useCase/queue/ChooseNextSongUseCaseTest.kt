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

class ChooseNextSongUseCaseTest {

    lateinit var sut: ChooseNextSongUseCase
    lateinit var repository: FakePlayerQueueRepository

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        repository = FakePlayerQueueRepository()
        sut = ChooseNextSongUseCase(repository)

        repository.fakeSetQueueItems(listOf(FakeQueueItem.ITEM_0))
    }

    @Test
    fun `next song has first position`() = runTest {

        val firstSong = FakeSong.SONG_1

        sut.invoke(firstSong)

        val expectedList = listOf(
            FakeQueueItem.ITEM_1.copy(position = 0, id = 0),
            FakeQueueItem.ITEM_0.copy(position = 1)
        )
        val currentList = repository.getFakeQueueItems()

        assertThat(currentList).containsExactlyElementsIn(expectedList).inOrder()
    }

}