package com.example.sound.Presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.SongsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val songRepository: SongRepository
) : ViewModel() {
    private var loadSongsJob: Job? = null
    private val _songsUiState = MutableStateFlow<SongsUiState>(SongsUiState.Loading)
    val songsUiState = _songsUiState.asStateFlow()
    fun permissionDenied() {
        loadSongsJob?.cancel()
        loadSongsJob = null
        _songsUiState.value = SongsUiState.PermissionDenied
    }

    fun setSongsUiState(){
        loadSongsJob?.cancel()
        loadSongsJob = null
        _songsUiState.value = SongsUiState.Loading
    }
    fun loadSongs() {
        loadSongsJob?.cancel()
        if (_songsUiState.value is SongsUiState.PermissionDenied) {
            return
        }
        _songsUiState.value = SongsUiState.Loading
        loadSongsJob = viewModelScope.launch {
            try {
                songRepository.loadSongs()
                songRepository.songs.first { songs ->
                    songs.isNotEmpty() //:TODO если список null загрузка бесконечна
                }
                _songsUiState.value = SongsUiState.Success
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _songsUiState.value = SongsUiState.Error(exception.toString())
            }
        }
    }
}
