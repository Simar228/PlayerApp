package com.example.sound.Presentation.songQueue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.model.toSong
import com.example.sound.Domain.repository.PlayerQueueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SongQueueViewModel @Inject constructor(
    private val playerQueueRepository: PlayerQueueRepository
) : ViewModel() {
    private val _songQueue = MutableStateFlow<List<QueueItemUi>>(emptyList())
    val sonqQueue = _songQueue.asStateFlow()


    init {
        viewModelScope.launch {
            _songQueue.value = playerQueueRepository.getQueue()
                .sortedBy { it.position }
                .map {
                    QueueItemUi(
                        song = it.toSong(),
                        queueItemId = UUID.randomUUID().toString()
                    )
                }
            playerQueueRepository.observeQueue().collect { queue ->
                _songQueue.value = queue.map { song ->
                    QueueItemUi(
                        song = song,
                        queueItemId = UUID.randomUUID().toString()
                    )
                }

            }
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
    val queueItemId: String,
    val song: Song
)

const val TAG = "SongQueueViewModel"