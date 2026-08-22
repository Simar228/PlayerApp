package com.example.sound.Domain.useCase.queue

import app.cash.turbine.test
import com.example.sound.Domain.model.FakeQueueItem
import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.repository.FakePlayerQueueRepository
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ObserveQueueUseCaseTest {

    lateinit var sut: ObserveQueueUseCase
    lateinit var repository: FakePlayerQueueRepository

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        repository = FakePlayerQueueRepository()
        sut = ObserveQueueUseCase(repository)
    }

    @Test
    fun `should emit queue items from repository when invoked`() = runTest {

        val testSong = FakeSong.SONG_0

        sut().test {
            assertEquals(emptyList<QueueItem>(), awaitItem())

            repository.insertSongAtTheEnd(testSong)

            val updatedList = awaitItem()
            assertThat(updatedList.size).isEqualTo(1)
            assertThat(updatedList).containsExactlyElementsIn(listOf(FakeQueueItem.create(song = testSong))).inOrder()

            cancelAndIgnoreRemainingEvents()
        }
    }
}
