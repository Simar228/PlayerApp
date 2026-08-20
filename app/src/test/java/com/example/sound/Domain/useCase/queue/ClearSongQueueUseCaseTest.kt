package com.example.sound.Domain.useCase.queue

import android.media.session.MediaSession
import com.example.sound.Domain.model.FakeQueueItem
import com.example.sound.Domain.repository.FakePlayerQueueRepository
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ClearSongQueueUseCaseTest {

    lateinit var sut: ClearSongQueueUseCase
    lateinit var repository: FakePlayerQueueRepository

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp(){
        repository = FakePlayerQueueRepository()
        repository.fakeSetQueueItems(listOf(
            FakeQueueItem.ITEM_0
        ))
        sut = ClearSongQueueUseCase(repository)
    }

    @Test
    fun `sut clear song queue item list`() = runTest {
        sut.invoke()

        val currentQueueItems = repository.getFakeQueueItems()

        assertThat(currentQueueItems).isEmpty()
    }

}