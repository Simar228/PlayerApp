package com.example.sound.Presentation.songHistory

import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.model.HistoryItem
import com.example.sound.Domain.repository.FakeHistoryQueueRepository
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HistorySongViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init groups repository history by day in dd MM format`() = runTest {
        val firstAugustTwelfth = historyItem(day = 12, songIndex = 0, position = 0)
        val secondAugustTwelfth = historyItem(day = 12, songIndex = 1, position = 1)
        val augustEleventh = historyItem(day = 11, songIndex = 2, position = 2)
        val repository = FakeHistoryQueueRepository(
            initialHistory = listOf(
                firstAugustTwelfth,
                secondAugustTwelfth,
                augustEleventh,
            )
        )

        val sut = HistorySongViewModel(repository)
        advanceUntilIdle()

        assertThat(sut.historyQueue.value.keys)
            .containsExactly("12.08", "11.08")
            .inOrder()
        assertThat(sut.historyQueue.value["12.08"])
            .containsExactly(firstAugustTwelfth, secondAugustTwelfth)
            .inOrder()
        assertThat(sut.historyQueue.value["11.08"])
            .containsExactly(augustEleventh)
    }

    @Test
    fun `repository update after init replaces grouped history`() = runTest {
        val repository = FakeHistoryQueueRepository()
        val sut = HistorySongViewModel(repository)
        val septemberSecond = HistoryItem(
            song = FakeSong.SONG_0,
            playedAt = LocalDate.of(2026, 9, 2)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            position = 0,
        )

        repository.setHistoryQueue(listOf(septemberSecond))
        advanceUntilIdle()

        assertThat(sut.historyQueue.value)
            .containsExactly("02.09", listOf(septemberSecond))
    }

    private fun historyItem(
        day: Int,
        songIndex: Int,
        position: Int,
    ): HistoryItem {
        val song = listOf(FakeSong.SONG_0, FakeSong.SONG_1, FakeSong.SONG_2)[songIndex]
        return HistoryItem(
            song = song,
            playedAt = LocalDate.of(2026, 8, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            position = position,
        )
    }
}
