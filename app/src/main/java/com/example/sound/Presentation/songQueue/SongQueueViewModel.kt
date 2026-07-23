package com.example.sound.Presentation.songQueue

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.model.toSong
import com.example.sound.Domain.repository.PlayerQueueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongQueueViewModel @Inject constructor(
    private val playerQueueRepository: PlayerQueueRepository
) : ViewModel() {
    private val _songQueue = MutableStateFlow<List<Song>>(emptyList())
    val sonqQueue = _songQueue.asStateFlow()


    init {
        viewModelScope.launch {
            _songQueue.value = playerQueueRepository.getQueue()
                .sortedBy { it.position }
                .map { it.toSong() }
            playerQueueRepository.observeQueue().collect { queue ->
                _songQueue.value = queue

            }
        }
    }

    fun clearSongQueue() {
        viewModelScope.launch {
            playerQueueRepository.clearQueue()
        }
    }

    fun addSongToQueue(song: Song) {
        viewModelScope.launch {
            playerQueueRepository.insertSong(song)
        }
    }

    fun chooseNextSong(song: Song) {
        viewModelScope.launch {
            playerQueueRepository.insertSongByIndex(
                song = song,
                position = 0
            )
        }
    }


}

const val TAG = "SongQueueViewModel"