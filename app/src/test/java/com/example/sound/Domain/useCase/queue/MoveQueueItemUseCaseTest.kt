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
            FakeQueueItem.create(),
            FakeQueueItem.create(id = 1, song = FakeSong.SONG_1, position = 1)
        )
    }

    @Test
    fun `if from index is not in current queue, list is not change`() {


        val currentQueueItemList = sut(expectedQueueItemList, 3, 1)

        assertThat(currentQueueItemList).isEqualTo(expectedQueueItemList)
    }

    @Test
    fun `if list is empty`() {
        val currentQueueItemList = sut(emptyList(), 0, 1)

        assertThat(currentQueueItemList).isEqualTo(listOf<QueueItem>())
    }

    @Test
    fun `if toIndex is not in current queue, list is not change`() {

        val currentQueueItemList = sut(expectedQueueItemList, 1, 3)

        assertThat(currentQueueItemList).isEqualTo(expectedQueueItemList)

    }

    @Test
    fun `fromIndex toIndex change list`() {

        val currentQueueItemList = sut(expectedQueueItemList, 1, 0)

        val changedList = listOf(
            FakeQueueItem.create(id = 1, song = FakeSong.SONG_1, position = 1),
            FakeQueueItem.create(position = 0, id = 0, song = FakeSong.SONG_0),
        )

        assertThat(currentQueueItemList).isEqualTo(changedList)

    }


}