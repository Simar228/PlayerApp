package com.example.sound.Data.repository

import com.example.sound.Data.historyQueue.FakeHistoryQueueDao
import com.example.sound.Domain.model.FakeSong
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class HistoryQueueRepositoryImplTest {

    lateinit var sut: HistoryQueueRepositoryImpl
    lateinit var historyQueueDao: FakeHistoryQueueDao

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        historyQueueDao = FakeHistoryQueueDao()
        sut = HistoryQueueRepositoryImpl(historyQueueDao)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Add History Queue Item`() = runTest {
        historyQueueDao.setHistoryQueueSong(listOf(FakeSong.SONG_0))
        val song = FakeSong.SONG_1

        sut.addHistoryItem(song)

        assertThat(historyQueueDao.historyQueueSong.value).containsExactlyElementsIn(
            listOf(
                FakeSong.SONG_1,
                FakeSong.SONG_0
            )
        )
            .inOrder()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `addHistoryItem does not duplicate song when it is already first`() = runTest {
        val song = FakeSong.SONG_0
        historyQueueDao.setHistoryQueueSong(listOf(song))

        sut.addHistoryItem(song)

        assertThat(historyQueueDao.historyQueueSong.value).containsExactlyElementsIn(
            listOf(song)
        )
    }

    @Test
    fun `observeHistoryQueue emits current history`() = runTest {
        historyQueueDao.setHistoryQueueSong(
            listOf(
                FakeSong.SONG_0,
                FakeSong.SONG_1
            )
        )

        val result = sut.observeHistoryQueue().first()

        assertThat(result)
            .containsExactly(
                FakeSong.SONG_0,
                FakeSong.SONG_1
            )
            .inOrder()
    }

    @Test
    fun `length of History less or equal 100`() = runTest {
        historyQueueDao.setHistoryQueueSong(
            List(99) { FakeSong.SONG_0 } + List(1) { FakeSong.SONG_1 }
        )

        sut.addHistoryItem(FakeSong.SONG_2)

        val history = historyQueueDao.historyQueueSong.value

        assertThat(history).hasSize(100)

        assertThat(history).containsExactlyElementsIn(
            listOf(FakeSong.SONG_2) +
                    List(99) { FakeSong.SONG_0 }
        ).inOrder()
    }

    @Test
    fun `adding item to 99 item history results in 100 items`() = runTest {
        historyQueueDao.setHistoryQueueSong(
            List(99) { FakeSong.SONG_0 }
        )

        sut.addHistoryItem(FakeSong.SONG_1)

        val history = historyQueueDao.historyQueueSong.value

        assertThat(history).hasSize(100)
        assertThat(history.first()).isEqualTo(FakeSong.SONG_1)
    }

    @Test
    fun `addHistoryItem to empty history does not crash`() = runTest {
        historyQueueDao.setHistoryQueueSong(emptyList())

        sut.addHistoryItem(FakeSong.SONG_0)

        assertThat(historyQueueDao.historyQueueSong.value)
            .containsExactly(FakeSong.SONG_0)
    }
}