package com.example.sound.Data.repository

import com.example.sound.Data.historyQueue.FakeHistoryQueueDao
import com.example.sound.Domain.model.FakeSong
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class HistoryQueueRepositoryImplTest {

    lateinit var repository: HistoryQueueRepositoryImpl
    lateinit var historyQueueDao: FakeHistoryQueueDao

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        repository = HistoryQueueRepositoryImpl(historyQueueDao)
    }

    @Test
    fun `Add History Queue Item`() = runTest {
        historyQueueDao.setHistoryQueueSong(listOf(FakeSong.SONG_0))
        val song = FakeSong.SONG_1

        repository.addHistoryItem(song)

        assertThat(historyQueueDao.historyQueueSong).containsExactlyElementsIn(
            listOf(
                FakeSong.SONG_1, FakeSong.SONG_0
            )
        )
            .inOrder()
    }

}