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
class PlayerQueueRepositoryImplTest {

    @get:Rule
    val dbRule = InMemoryDatabaseRule(AppDatabase::class.java)

    private lateinit var sut: PlayerQueueRepositoryImpl

    @Before
    fun setUp() {

        val database = dbRule.database
        val queueDao = database.queueDao()

        sut = PlayerQueueRepositoryImpl(
            queueDao = queueDao,
            database = database
        )
    }

    @Test
    fun insertSongAtTheEnd_shouldIncrementPosition() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)

        val result = sut.observeQueue().first()

        assertThat(result).hasSize(2)
        assertThat(result[0].position).isEqualTo(0)
        assertThat(result[1].position).isEqualTo(1)
    }

    @Test
    fun clearQueue_shouldLeaveDatabaseEmpty() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)

        sut.clearQueue()

        val result = sut.observeQueue().first()
        assertThat(result).isEmpty()
    }

    @Test
    fun observeQueue_shouldCorrectlyMapEntitiesToDomainAndEmitUpdates() = runTest {
        // 1. Добавляем песню в базу данных
        sut.insertSongAtTheEnd(FakeSong.SONG_0)

        // 2. Считываем первую эмиссию из Flow через .first()
        val initialEmit = sut.observeQueue().first()

        assertThat(initialEmit).hasSize(1)
        // Проверяем, что маппинг в доменную модель прошел успешно
        assertThat(initialEmit.first().song.title).isEqualTo(FakeSong.SONG_0.title)

        // 3. Добавляем еще одну песню, чтобы проверить реактивность Flow
        sut.insertSongAtTheEnd(FakeSong.SONG_1)

        // Снова берем актуальный снимок из Flow
        val updatedEmit = sut.observeQueue().first()

        // Проверяем, что Flow отреагировал на изменения в БД и прислал обновленный список
        assertThat(updatedEmit).hasSize(2)
        assertThat(updatedEmit[1].song.title).isEqualTo(FakeSong.SONG_1.title)
    }

    @Test
    fun saveQueueOrder_shouldRearrangeItemsInDatabaseAccordingToProvidedIds() = runTest {
        // 1. Наполняем базу тремя песнями
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)
        sut.insertSongAtTheEnd(FakeSong.SONG_2)

        // Получаем текущую очередь, чтобы узнать точные сгенерированные ID из базы данных
        val originalQueue = sut.observeQueue().first()
        val id0 = originalQueue[0].id
        val id1 = originalQueue[1].id
        val id2 = originalQueue[2].id

        // 2. Имитируем перемещение: хотим, чтобы порядок стал [ID_2, ID_0, ID_1]
        val newOrderOfIds = listOf(id2, id0, id1)

        // Вызываем тестируемый метод
        sut.saveQueueOrder(newOrderOfIds)

        // 3. Читаем измененную очередь
        val rearrangedQueue = sut.observeQueue().first()

        // Проверяем, что песни теперь идут в новом порядке
        assertThat(rearrangedQueue).hasSize(3)
        assertThat(rearrangedQueue[0].id).isEqualTo(id2)
        assertThat(rearrangedQueue[1].id).isEqualTo(id0)
        assertThat(rearrangedQueue[2].id).isEqualTo(id1)
    }


    @Test
    fun deleteQueueItemById_middleItem_shouldReindexRemaining() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0) // pos 0
        sut.insertSongAtTheEnd(FakeSong.SONG_1) // pos 1
        sut.insertSongAtTheEnd(FakeSong.SONG_2) // pos 2

        val initialQueue = sut.observeQueue().first()
        val idToDelete = initialQueue[1].id // Берем ID второго трека (SONG_1)

        sut.deleteQueueItemById(idToDelete)

        val result = sut.observeQueue().first()

        assertThat(result).hasSize(2)
        // SONG_0 осталась на месте
        assertThat(result[0].song).isEqualTo(FakeSong.SONG_0)
        assertThat(result[0].position).isEqualTo(0)
        // SONG_2 сместилась с позиции 2 на позицию 1!
        assertThat(result[1].song).isEqualTo(FakeSong.SONG_2)
        assertThat(result[1].position).isEqualTo(1)
    }

    @Test
    fun insertSongAtTheStart_shouldShiftExistingPositions() = runTest {

        // Сначала добавляем элементы в конец: SONG_0 (pos 0), SONG_1 (pos 1)
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.insertSongAtTheEnd(FakeSong.SONG_1)

        // Вставляем SONG_2 в начало
        sut.insertSongAtTheStart(FakeSong.SONG_2)

        val result = sut.observeQueue().first()

        assertThat(result).hasSize(3)
        // Проверяем, что SONG_2 встала на позицию 0
        assertThat(result[0].song).isEqualTo(FakeSong.SONG_2)
        assertThat(result[0].position).isEqualTo(0)
        // Проверяем сдвиг остальных
        assertThat(result[1].song).isEqualTo(FakeSong.SONG_0)
        assertThat(result[1].position).isEqualTo(1)
    }


    @Test
    fun clearQueue_shouldDeleteAllItemsFromDatabase() = runTest {
        sut.insertSongAtTheEnd(FakeSong.SONG_0)
        sut.clearQueue()

        val currentQueue = sut.observeQueue().first()
        assertThat(currentQueue).isEmpty()
    }
}