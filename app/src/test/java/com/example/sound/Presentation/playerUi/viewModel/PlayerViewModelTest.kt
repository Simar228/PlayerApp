package com.example.sound.Presentation.playerUi.viewModel

import com.example.sound.Domain.model.FakeSong
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import com.example.sound.Presentation.playerUi.PlayerConnectionState
import com.example.sound.Presentation.playerUi.PlayerUIEvent
import com.example.sound.Presentation.playerUi.PlayerUiState
import com.example.sound.utill.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.atLeast
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 1. Создаем моки внешних зависимостей
    private val playbackTransitionRepository: PlaybackTransitionRepository = mock()
    private val playerControllerFactory: PlayerControllerFactory = mock()
    private val playerController: PlayerController = mock()

    // 2. Создаем реальный StateFlow для подмены состояния плеера
    private val fakeControllerState = MutableStateFlow(PlayerUiState())

    private lateinit var sut: PlayerViewModel
    private var capturedOnControllerReady: (() -> Unit)? = null

    @Before
    fun setUp() {
        // Подкладываем наш StateFlow вместо реального внутри контроллера
        whenever(playerController.mediaControllerState).thenReturn(fakeControllerState)

        // Настраиваем фабрику: при вызове create() перехватываем лямбду обратного вызова
        whenever(playerControllerFactory.create(any())).thenAnswer { invocation ->
            capturedOnControllerReady = invocation.getArgument(0) as () -> Unit
            playerController
        }

        // Инициализируем ViewModel (в этот момент отработает блок init и вызовется connect())
        sut = PlayerViewModel(playbackTransitionRepository, playerControllerFactory)
    }

    @Test
    fun `init should immediately call connect on playerController`() {
        // Проверяем, что ViewModel сразу пытается установить соединение
        verify(playerController).connect()
    }

    @Test
    fun `sendSong when connection is Connecting should cache request and show song stub`() = runTest {
        // Устанавливаем состояние "Подключение"
        fakeControllerState.value = PlayerUiState(connectionState = PlayerConnectionState.Connecting)
        val song = FakeSong.SONG_0

        sut.sendSong(song = song)

        // Должен обновиться UI (заглушка), но реальное воспроизведение в репозитории еще не началось
        verify(playerController).showSelectedSong(song)
        verifyNoInteractions(playbackTransitionRepository)
    }

    @Test
    fun `when controller becomes Ready, any pending playback request must be executed`() = runTest {
        // 1. Пользователь нажимает на трек, пока плеер еще грузится
        fakeControllerState.value = PlayerUiState(connectionState = PlayerConnectionState.Connecting)
        val song = FakeSong.SONG_1
        sut.sendSong(song = song)

        // 2. Имитируем успешное подключение сервиса Media3
        fakeControllerState.value = PlayerUiState(connectionState = PlayerConnectionState.Ready)
        capturedOnControllerReady?.invoke() // Симулируем вызов handleControllerReady()
        advanceUntilIdle()

        // Проверяем, что отложенная песня автоматически отправилась на проигрывание
        verify(playbackTransitionRepository).startPlayback(
            song = song,
            defaultQueueSongs = null,
            queueItemId = null
        )
    }

    @Test
    fun `sendSong when connection is Ready should play song instantly`() = runTest {
        // Плеер уже готов
        fakeControllerState.value = PlayerUiState(connectionState = PlayerConnectionState.Ready)
        val song = FakeSong.SONG_2

        sut.sendSong(song = song)
        advanceUntilIdle()

        // Воспроизведение должно запуститься сразу же
        verify(playbackTransitionRepository).startPlayback(song, null, null)
    }

    @Test
    fun `sendEvent should forward corresponding UI events to playerController`() {
        sut.sendEvent(PlayerUIEvent.NextSong)
        verify(playerController).next()

        sut.sendEvent(PlayerUIEvent.PreviousSong)
        verify(playerController).previous()

        sut.sendEvent(PlayerUIEvent.Play)
        verify(playerController).play()

        sut.sendEvent(PlayerUIEvent.Pause)
        verify(playerController).pause()

        sut.sendEvent(PlayerUIEvent.SeekTo(4200L))
        verify(playerController).seekTo(4200L)
    }

    @Test
    fun `startPositionUpdates should poll player position periodically`() = runTest {
        sut.startPositionUpdates()

        // Сдвигаем виртуальное время корутин на 510 мс вперед
        advanceTimeBy(510L)

        // За это время метод обновления должен вызваться минимум 2 раза (каждые 250мс)
        verify(playerController, atLeast(2)).updatePosition()

        sut.stopPositionUpdates()
    }

    @Test
    fun `sendSong when connection is Error should not play song and not update controller`() = runTest {
        // Имитируем ошибку подключения плеера
        fakeControllerState.value = PlayerUiState(
            connectionState = PlayerConnectionState.Error(Exception("Media3 Connection Failed"))
        )
        val song = FakeSong.SONG_0

        sut.sendSong(song = song)
        advanceUntilIdle()

        // Проверяем, что методы воспроизведения НЕ вызывались
        verifyNoInteractions(playbackTransitionRepository)
        verify(playerController, never()).showSelectedSong(any())
    }

    @Test
    fun `ViewModel cleared should stop position updates and release playerController`() {
        sut.startPositionUpdates()

        // Вызываем скрытый метод onCleared() через рефлексию
        val method = PlayerViewModel::class.java.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(sut)

        // Проверяем, что контроллер освобожден
        verify(playerController).release()
    }


}
