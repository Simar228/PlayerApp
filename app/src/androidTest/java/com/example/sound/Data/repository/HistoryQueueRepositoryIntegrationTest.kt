package com.example.sound.Data.repository

import app.cash.turbine.test
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Data.local.historyQueue.HistoryQueueDao
import com.example.sound.Domain.model.FakeSong
import com.example.sound.utill.InMemoryDatabaseRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HistoryQueueRepositoryImplIntegrationTest {
    @get:Rule
    val dbRule = InMemoryDatabaseRule(AppDatabase::class.java)

    private lateinit var sut: HistoryQueueRepositoryImpl
    private lateinit var dao: HistoryQueueDao

    @Before
    fun setUp() {
        val database = dbRule.database
        dao = database.historyQueueDao()
        sut = HistoryQueueRepositoryImpl(
            historyQueueDao = dao
        )
    }

    @Test
    fun observeHistoryQueue_emitsUpdatedHistory() = runTest {
        sut.observeHistoryQueue().test {
            assertThat(awaitItem()).isEmpty()
            sut.addHistoryItem(
                song = FakeSong.SONG_0,
                playedAt = NOW
            )
            assertThat(awaitItem().first().song).isEqualTo(FakeSong.SONG_0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addHistoryItem_savesItemCorrectly() = runTest {
        sut.addHistoryItem(
            song = FakeSong.SONG_0,
            playedAt = NOW
        )

        val result = dao.observeHistoryQueueItems().first()

        assertThat(result).hasSize(1)

        val item = result.first()

        assertThat(item.song).isEqualTo(FakeSong.SONG_0)
        assertThat(item.playedAt).isEqualTo(NOW)
        assertThat(item.position).isEqualTo(0)
    }

    @Test
    fun addHistoryItem_keepsNewestSongFirst() = runTest {
        sut.addHistoryItem(
            song = FakeSong.SONG_0,
            playedAt = NOW
        )

        sut.addHistoryItem(
            song = FakeSong.SONG_1,
            playedAt = NOW + 1
        )

        val result = dao.observeHistoryQueueItems().first()

        assertThat(result.map { it.song })
            .containsExactly(
                FakeSong.SONG_1,
                FakeSong.SONG_0
            )
            .inOrder()

        assertThat(result.map { it.position })
            .containsExactly(0, 1)
            .inOrder()
    }

    private companion object {
        const val NOW = 1_725_000_000_000L
    }
}