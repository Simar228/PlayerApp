package com.example.sound.Presentation.songQueue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Data.local.queue.QueueItemEntity
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.model.toQueueItem
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
    private var _songQueue = MutableStateFlow<List<QueueItem>>(emptyList())
    val sonqQueue = _songQueue.asStateFlow()

    init {
        viewModelScope.launch {
            playerQueueRepository.observeQueue().collect {
                _songQueue.value = it
            }
        }
    }

    fun clearSongQueue(){
        viewModelScope.launch {
            playerQueueRepository.clearQueue()
        }
    }
    fun addSongToQueue(song: Song){
        viewModelScope.launch {
            playerQueueRepository.insertQueueItem(song.toQueueItem(0))
        }
    }
    fun chooseNextSong(song: Song) {
        viewModelScope.launch {
            playerQueueRepository.insertSongByIndex(
                song = song,
                index = 0
            )
        }
    }

}