package com.example.sound.Domain.useCase.queue

import com.example.sound.Domain.model.FakeQueueItem
import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.model.QueueItem
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class MoveQueueItemUseCaseTest {

    lateinit var sut: MoveQueueItemUseCase
    lateinit var expectedQueueItemList: List<QueueItem>

    @Before
    fun setUp() {
        sut = MoveQueueItemUseCase()
        expectedQueueItemList = listOf(
            FakeQueueItem.ITEM_0,
            FakeQueueItem.ITEM_1
        )
    }

    @Test
    fun `if from index is not in current queue, list is not change`() {


        val currentQueueItemList = sut.invoke(expectedQueueItemList, 3, 1)

        assertThat(currentQueueItemList).isEqualTo(expectedQueueItemList)
    }

    @Test
    fun `if list is empty`() {
        val currentQueueItemList = sut.invoke(emptyList(), 0, 1)

        assertThat(currentQueueItemList).isEqualTo(listOf<QueueItem>())
    }

    @Test
    fun `if toIndex is not in current queue, list is not change`() {

        val currentQueueItemList = sut.invoke(expectedQueueItemList, 1, 3)

        assertThat(currentQueueItemList).isEqualTo(expectedQueueItemList)

    }

    @Test
    fun `fromIndex toIndex change list`() {

        val currentQueueItemList = sut.invoke(expectedQueueItemList, 1, 0)

        val changedList = listOf(
            FakeQueueItem.ITEM_1,
            FakeQueueItem.ITEM_0,
        )

        assertThat(currentQueueItemList).isEqualTo(changedList)

    }


}