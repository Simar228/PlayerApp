package com.example.sound.Domain.useCase.queue

import com.example.sound.utill.MainDispatcherRule
import com.example.sound.Domain.model.FakeQueueItem
import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.repository.FakePlayerQueueRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SaveQueueOrderUseCaseTest {

    private lateinit var sut: SaveQueueOrderUseCase
    private lateinit var repository: FakePlayerQueueRepository

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        repository = FakePlayerQueueRepository()
        sut = SaveQueueOrderUseCase(repository)
    }

    @Test
    fun `Use Case returns only QueueItem's ID`() = runTest {
        val queueItems = listOf(
            FakeQueueItem.ITEM_1,
            FakeQueueItem.ITEM_0,
        )

        sut.invoke(queueItems)

        val expectedListIds = listOf(1L, 0L)
        val currentListIds = repository.getQueueOfIds()


        assertThat(currentListIds).containsExactlyElementsIn(expectedListIds).inOrder()

    }

}