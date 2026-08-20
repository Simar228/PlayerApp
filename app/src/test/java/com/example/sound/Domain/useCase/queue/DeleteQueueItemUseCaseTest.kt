package com.example.sound.Domain.useCase.queue

import com.example.sound.Domain.model.FakeQueueItem
import com.example.sound.Domain.repository.FakePlayerQueueRepository
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeleteQueueItemUseCaseTest {

    lateinit var cut: DeleteQueueItemUseCase
    lateinit var repository: FakePlayerQueueRepository

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        repository = FakePlayerQueueRepository()
        cut = DeleteQueueItemUseCase(repository)
        repository.fakeSetQueueItems(
            listOf(
                FakeQueueItem.ITEM_0,
                FakeQueueItem.ITEM_1,
            )
        )
    }

    @Test
    fun `use case delete QueueItem by id`() = runTest {
        val expectedList = listOf(FakeQueueItem.ITEM_1.copy(position = 0))

        cut.invoke(0)

        val currentQueueItemList = repository.getFakeQueueItems()
        assertThat(currentQueueItemList).containsExactlyElementsIn(expectedList).inOrder()
    }


}