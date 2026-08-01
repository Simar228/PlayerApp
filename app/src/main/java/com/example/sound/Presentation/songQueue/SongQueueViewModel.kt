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
        viewModelScope.launch {
            playerQueueRepository.saveQueue(
                _songQueue.value.mapIndexed { index, queueItemUi ->
                    QueueItem(
                        id = queueItemUi.queueItemId,
                        song = queueItemUi.song,
                        position = index
                    )

                }
            )
        }
    }

    fun moveQueueItem(
        fromIndex: Int,
        toIndex: Int
    ) {
        val targetSong = _songQueue.value[fromIndex]
        _songQueue.value = _songQueue.value.toMutableList().apply {
            removeAt(fromIndex)
            add(toIndex, targetSong)
        }
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