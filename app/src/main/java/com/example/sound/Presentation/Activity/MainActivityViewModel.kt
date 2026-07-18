package com.example.sound.Presentation.Activity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.SongsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        _songsUiState.value = SongsUiState.PermissionDenied
    }

    fun loadSongs() {
        if (loadSongsJob?.isActive == true || _songsUiState.value is SongsUiState.Success) {
            return
        }
        loadSongsJob = viewModelScope.launch {
            Log.d(TAG, "loadSongs() started")
            _songsUiState.value = SongsUiState.Loading
            try {
                val loadedSongs = withContext(Dispatchers.IO) {
                    songRepository.getSong()
                }
                Log.d(
                    TAG,
                    "loadSongs() returned ${loadedSongs.size} songs"
                )
                _songsUiState.value = SongsUiState.Success(loadedSongs)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _songsUiState.value = SongsUiState.Error(exception.toString())
            }
        }
    }
}

const val TAG = "MainActivityViewModel"