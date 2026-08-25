package com.example.sound.Presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Domain.useCase.mainActivity.LoadSongUseCase
import com.example.sound.Presentation.SongsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val loadSongUseCase: LoadSongUseCase,
) : ViewModel() {
    private var loadSongsJob: Job? = null
    private val _songsUiState = MutableStateFlow<SongsUiState>(SongsUiState.Loading)
    val songsUiState = _songsUiState.asStateFlow()
    fun permissionDenied() {
        loadSongsJob?.cancel()
        loadSongsJob = null
        _songsUiState.value = SongsUiState.PermissionDenied
    }

    fun setSongsUiState() {
        loadSongsJob?.cancel()
        loadSongsJob = null
        _songsUiState.value = SongsUiState.Loading
    }

    fun loadSongs() {
        loadSongsJob?.cancel()
        loadSongsJob = viewModelScope.launch {
            loadSongUseCase(_songsUiState)
        }
    }
}
