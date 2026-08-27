package com.example.sound.Data.repository

import com.example.sound.Data.historyQueue.FakeHistoryQueueDao
import com.example.sound.Data.local.historyQueue.HistoryQueueItemEntity
import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.model.HistoryItem
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
        historyQueueDao.setHistoryQueueItemEntity(
            listOf(
                HistoryQueueItemEntity(
                    position = 0,
                    playedAt = NOW,
                    song = FakeSong.SONG_0
                )
            )
        )
        val song = FakeSong.SONG_1

        sut.addHistoryItem(song, NOW)

        assertThat(historyQueueDao.historyQueueItemEntity).containsExactlyElementsIn(
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
        historyQueueDao.setHistoryQueueItemEntity(
            listOf(
                HistoryQueueItemEntity(
                    position = 0,
                    playedAt = NOW,
                    song = song
                )
            )
        )

        sut.addHistoryItem(song, NOW)

        assertThat(historyQueueDao.historyQueueItemEntity).containsExactlyElementsIn(
            listOf(song)
        )
    }

    @Test
    fun `observeHistoryQueue emits current history`() = runTest {
        val listHistoryQueueItemEntity = listOf(
            HistoryQueueItemEntity(
                position = 0,
                playedAt = NOW,
                song = FakeSong.SONG_0
            ),
            HistoryQueueItemEntity(
                position = 1,
                playedAt = NOW,
                song = FakeSong.SONG_1
            ),
        )

        historyQueueDao.setHistoryQueueItemEntity(
            listHistoryQueueItemEntity
        )

        val result = sut.observeHistoryQueue().first()

        assertThat(result)
            .containsExactly(
                HistoryItem(
                    song = FakeSong.SONG_0,
                    playedAt = NOW
                ),
                HistoryItem(
                    song = FakeSong.SONG_1,
                    playedAt = NOW
                )
            )
            .inOrder()
    }

    @Test
    fun `length of History less or equal 100`() = runTest {
        historyQueueDao.setHistoryQueueItemEntity(
            List(99) { index ->
                HistoryQueueItemEntity(
                    position = index,
                    playedAt = NOW,
                    song = FakeSong.SONG_0,
                )
            } + HistoryQueueItemEntity(
                position = 99,
                playedAt = NOW,
                song = FakeSong.SONG_1,
            )
        )

        sut.addHistoryItem(
            FakeSong.SONG_2, NOW
        )

        val history = historyQueueDao.historyQueueItemEntity

        assertThat(history).hasSize(100)

        assertThat(history).containsExactlyElementsIn(
            listOf(FakeSong.SONG_2) +
                    List(99) { FakeSong.SONG_0 }
        )
            .inOrder()
    }

    @Test
    fun `adding item to 99 item history results in 100 items`() = runTest {
        historyQueueDao.setHistoryQueueItemEntity(
            List(99) {
                HistoryQueueItemEntity(
                    position = 1,
                    playedAt = NOW,
                    song = FakeSong.SONG_0
                )
            }
        )
        sut.addHistoryItem(FakeSong.SONG_1, NOW)

        val history = historyQueueDao.historyQueueItemEntity

        assertThat(history).hasSize(100)
        assertThat(history.first()).isEqualTo(FakeSong.SONG_1)

    }

    @Test
    fun `addHistoryItem to empty history does not crash`() = runTest {
        historyQueueDao.setHistoryQueueItemEntity(emptyList())

        sut.addHistoryItem(FakeSong.SONG_0, NOW)

        assertThat(historyQueueDao.historyQueueItemEntity)
            .containsExactly(FakeSong.SONG_0)
    }

    private companion object {
        const val NOW = 1_725_000_000_000L
        const val YESTERDAY = NOW - 86_400_000L
        const val TWO_DAYS_AGO = NOW - 2 * 86_400_000L
    }
}

