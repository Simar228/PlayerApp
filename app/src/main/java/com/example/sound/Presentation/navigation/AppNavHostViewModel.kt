package com.example.sound.Presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppNavHostViewModel @Inject constructor(
    songRepository: SongRepository
) : ViewModel() {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs = _songs.asStateFlow()

    init {
        viewModelScope.launch {
            songRepository.songs.collect { songs ->
                _songs.value = songs
            }
        }
    }
}