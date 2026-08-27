package com.example.sound.Data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sound.Data.local.AppDatabase
import com.example.sound.Domain.model.FakeSong
import com.example.sound.utill.InMemoryDatabaseRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PlayerQueueRepositoryIntegrationTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule(AppDatabase::class.java)

    private lateinit var sut: PlayerQueueRepositoryImpl

    @Before
    fun setUp() {
        val database = dbRule.database
        sut = PlayerQueueRepositoryImpl(
            queueDao = database.queueDao(),
            database = database,
        )
    }

    @Test
    fun observeQueue_whenQueueIsEmpty_returnsEmptyList() = runTest {
        val result = sut.observeQueue().first()

        assertThat(result).isEmpty()
    }

    @Test
    fun insertSongAtTheEnd_appendsSongsWithConsecutivePositions() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)

        val result = sut.observeQueue().first()

        assertThat(result.map { it.song }).containsExactly(
            FakeSong.SONG_0,
            FakeSong.SONG_1,
        ).inOrder()
        assertThat(result.map { it.position }).containsExactly(0, 1).inOrder()
        assertThat(result.map { it.id }).doesNotContain(0L)
    }

    @Test
    fun saveQueueOrder_reordersItemsByIdAndUpdatesPositions() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)
        sut.insertSongAtTheEnd(FakeSong.SONG_2)
        val originalQueue = sut.observeQueue().first()

        sut.saveQueueOrder(
            listOf(originalQueue[2].id, originalQueue[0].id, originalQueue[1].id)
        )

        val result = sut.observeQueue().first()
        assertThat(result.map { it.song }).containsExactly(
            FakeSong.SONG_2,
            FakeSong.SONG_0,
            FakeSong.SONG_1,
        ).inOrder()
        assertThat(result.map { it.position }).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun saveQueueOrder_withUnknownAndRepeatedIds_ignoresThemAndKeepsOmittedItems() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)
        sut.insertSongAtTheEnd(FakeSong.SONG_2)
        val originalQueue = sut.observeQueue().first()
        val movedId = originalQueue[2].id

        sut.saveQueueOrder(listOf(Long.MAX_VALUE, movedId, movedId))

        val result = sut.observeQueue().first()
        assertThat(result.map { it.song }).containsExactly(
            FakeSong.SONG_2,
            FakeSong.SONG_0,
            FakeSong.SONG_1,
        ).inOrder()
        assertThat(result.map { it.position }).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun deleteQueueItemById_removesMiddleItemAndReindexesRemainingItems() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)
        sut.insertSongAtTheEnd(FakeSong.SONG_2)
        val idToDelete = sut.observeQueue().first()[1].id

        sut.deleteQueueItemById(idToDelete)

        val result = sut.observeQueue().first()
        assertThat(result.map { it.song }).containsExactly(
            FakeSong.SONG_0,
            FakeSong.SONG_2,
        ).inOrder()
        assertThat(result.map { it.position }).containsExactly(0, 1).inOrder()
    }

    @Test
    fun deleteQueueItemById_withUnknownId_leavesQueueUnchanged() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)
        val originalQueue = sut.observeQueue().first()

        sut.deleteQueueItemById(Long.MAX_VALUE)

        assertThat(sut.observeQueue().first()).isEqualTo(originalQueue)
    }

    @Test
    fun insertSongAtTheStart_prependsSongAndShiftsExistingPositions() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)
        sut.insertSongAtTheStart(FakeSong.SONG_2)

        val result = sut.observeQueue().first()
        assertThat(result.map { it.song }).containsExactly(
            FakeSong.SONG_2,
            FakeSong.SONG_0,
            FakeSong.SONG_1,
        ).inOrder()
        assertThat(result.map { it.position }).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun clearQueue_removesAllItems() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)

        sut.clearQueue()

        assertThat(sut.observeQueue().first()).isEmpty()
    }
}
