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

        repository.fakeSetQueueItems(listOf(FakeQueueItem.create()))
    }

    @Test
    fun `next song has first position`() = runTest {

        val songToInsert = FakeSong.SONG_3

        sut(songToInsert)

        val currentList = repository.getFakeQueueItems()
        val expectedList = listOf(
            FakeQueueItem.create(song = FakeSong.SONG_3, id = 1),
            FakeQueueItem.create(position = 1)
        )

        assertThat(currentList).containsExactlyElementsIn(expectedList).inOrder()
    }

}