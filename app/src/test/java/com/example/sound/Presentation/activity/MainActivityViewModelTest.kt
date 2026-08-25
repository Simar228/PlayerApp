package com.example.sound.Presentation.activity

import com.example.sound.Domain.repository.FakeSongRepository
import com.example.sound.Domain.useCase.mainActivity.LoadSongUseCase
import com.example.sound.Presentation.SongsUiState
import com.example.sound.utill.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: MainActivityViewModel
    private lateinit var repository: FakeSongRepository

    @Before
    fun setUp() {
        repository = FakeSongRepository()
        sut = MainActivityViewModel(
            loadSongUseCase = LoadSongUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        sut.setSongsUiState()
    }

    @Test
    fun `permissionDenied updates state cancels loading and clears job`() {
        sut.loadSongs()
        val runningJob = getLoadSongsJob()

        sut.permissionDenied()

        assertThat(sut.songsUiState.value).isEqualTo(SongsUiState.PermissionDenied)
        assertThat(runningJob).isNotNull()
        assertThat(runningJob!!.isCancelled).isTrue()
        assertThat(getLoadSongsJob()).isNull()
    }

    @Test
    fun `setSongsUiState updates state cancels loading and clears job`() {
        sut.loadSongs()
        val runningJob = getLoadSongsJob()

        sut.setSongsUiState()

        assertThat(sut.songsUiState.value).isEqualTo(SongsUiState.Loading)
        assertThat(runningJob).isNotNull()
        assertThat(runningJob!!.isCancelled).isTrue()
        assertThat(getLoadSongsJob()).isNull()
    }

    @Test
    fun `loadSongs cancels previous job and invokes LoadSongUseCase again`() {
        sut.loadSongs()
        val previousJob = getLoadSongsJob()

        sut.loadSongs()
        val currentJob = getLoadSongsJob()

        assertThat(previousJob).isNotNull()
        assertThat(previousJob!!.isCancelled).isTrue()
        assertThat(currentJob).isNotNull()
        assertThat(currentJob).isNotSameInstanceAs(previousJob)
        assertThat(currentJob!!.isActive).isTrue()
        assertThat(repository.loadSongsCallCount).isEqualTo(2)
    }

    private fun getLoadSongsJob(): Job? {
        val field = MainActivityViewModel::class.java.getDeclaredField("loadSongsJob")
        field.isAccessible = true
        return field.get(sut) as Job?
    }
}
