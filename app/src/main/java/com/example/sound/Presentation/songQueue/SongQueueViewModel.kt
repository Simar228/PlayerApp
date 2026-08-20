package com.example.sound.Presentation.songQueue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.Domain.useCase.queue.ChooseNextSongUseCase
import com.example.sound.Domain.useCase.queue.ClearSongQueueUseCase
import com.example.sound.Domain.useCase.queue.DeleteQueueItemUseCase
import com.example.sound.Domain.useCase.queue.MoveQueueItemUseCase
import com.example.sound.Domain.useCase.queue.SaveQueueOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongQueueViewModel @Inject constructor(
    private val saveQueueOrderUseCase: SaveQueueOrderUseCase,
    private val moveQueueItemUseCase: MoveQueueItemUseCase,
    private val deleteQueueItemUseCase: DeleteQueueItemUseCase,
    private val clearSongQueueUseCase: ClearSongQueueUseCase,
    private val chooseNextSongUseCase: ChooseNextSongUseCase,
    private val playerQueueRepository: PlayerQueueRepository,
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
            saveQueueOrderUseCase(_songQueue.value)
        }
    }


    fun moveQueueItem(
        fromIndex: Int,
        toIndex: Int
    ) {
        _songQueue.value = moveQueueItemUseCase(_songQueue.value, fromIndex, toIndex)
    }


    fun deleteQueueItem(queueItemId: Long) {
        viewModelScope.launch {
            deleteQueueItemUseCase(queueItemId)
        }
    }

    fun clearSongQueue() {
        viewModelScope.launch {
            clearSongQueueUseCase()
        }
    }

    fun addSongToQueue(song: Song) {
        viewModelScope.launch {
            chooseNextSong(song)
        }
    }

    fun chooseNextSong(song: Song) {
        viewModelScope.launch {
            chooseNextSongUseCase(song)
        }
    }


}
