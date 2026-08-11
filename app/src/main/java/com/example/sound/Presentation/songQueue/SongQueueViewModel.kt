package com.example.sound.Presentation.songQueue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
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
    private val _songQueue = MutableStateFlow<List<QueueItem>>(emptyList())
    val songQueue = _songQueue.asStateFlow()


    init {
        viewModelScope.launch {
            playerQueueRepository.observeQueue().collect { queue ->
                _songQueue.value = queue
            }
        }
    }

    fun saveQueueOrder() {
        viewModelScope.launch {
            val queueItemIds = _songQueue.value.map { queueItem ->
                queueItem.id
            }
            playerQueueRepository.saveQueueOrder(queueItemIds)
        }
    }


    fun moveQueueItem(
        fromIndex: Int, toIndex: Int
    ) {
        val currentQueue = _songQueue.value

        if (fromIndex !in currentQueue.indices || toIndex !in currentQueue.indices) {
            return
        }

        val updatedQueue = currentQueue.toMutableList()
        val movedItem = updatedQueue.removeAt(fromIndex)
        updatedQueue.add(toIndex, movedItem)

        _songQueue.value = updatedQueue

    }


    fun deleteQueueItem(queueItemId: Long) {
        viewModelScope.launch {
            playerQueueRepository.deleteQueueItem(queueItemId)
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
            playerQueueRepository.insertSongByPosition(
                song = song, position = 0
            )
        }
    }


}
