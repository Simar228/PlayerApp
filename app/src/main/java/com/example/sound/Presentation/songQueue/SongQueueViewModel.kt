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
    private val _songQueue = MutableStateFlow<List<QueueItemUi>>(emptyList())
    val songQueue = _songQueue.asStateFlow()


    init {
        viewModelScope.launch {
            playerQueueRepository.observeQueue().collect { queue ->
                _songQueue.value = queue.map { queueItem ->
                    QueueItemUi(
                        song = queueItem.song,
                        queueItemId = queueItem.id
                    )
                }

            }
        }
    }

    fun saveQueueOrder() {
        val reorderedQueue = _songQueue.value.mapIndexed { index, queueItem ->
            QueueItem(
                id = queueItem.queueItemId,
                song = queueItem.song,
                position = index
            )
        }
        viewModelScope.launch {
            playerQueueRepository.saveQueue(reorderedQueue)
        }
    }


    fun moveQueueItem(
        fromIndex: Int,
        toIndex: Int
    ) {
        val currentQueue = _songQueue.value

        if (fromIndex !in currentQueue.indices ||
            toIndex !in currentQueue.indices
        ) {
            return
        }

        val updatedQueue = currentQueue.toMutableList()
        val movedItem = updatedQueue.removeAt(fromIndex)
        updatedQueue.add(toIndex, movedItem)

        _songQueue.value = updatedQueue

    }


    fun deleteSongByPosition(song: Song, position: Int) {
        viewModelScope.launch {
            playerQueueRepository.deleteSongByPosition(song, position)
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
                song = song,
                position = 0
            )
        }
    }


}

data class QueueItemUi(
    val queueItemId: Long,
    val song: Song
)

const val TAG = "SongQueueViewModel"