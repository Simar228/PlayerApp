package com.example.sound.Presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.SongsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun loadSongs() {
        if (loadSongsJob?.isActive == true) {
            return
        }
        loadSongsJob = viewModelScope.launch {
            _songsUiState.value = SongsUiState.Loading
            try {
                songRepository.observeSongs().collect { loadedSongs ->
                    _songsUiState.value = SongsUiState.Success(loadedSongs)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _songsUiState.value = SongsUiState.Error(exception.toString())
            }
        }
    }
}
