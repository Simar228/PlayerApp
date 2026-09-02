package com.example.sound.Presentation.songHistory

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sound.Data.local.historyQueue.toDate
import com.example.sound.Domain.model.HistoryItem
import com.example.sound.Domain.repository.HistoryQueueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class HistorySongViewModel @Inject constructor(
    private val historyQueueRepository: HistoryQueueRepository
) : ViewModel() {
    private val _historyQueue = MutableStateFlow(emptyMap<String, List<HistoryItem>>())
    val historyQueue = _historyQueue.asStateFlow()

    init {
        viewModelScope.launch {
            historyQueueRepository.observeHistoryQueue().collect { items ->
                _historyQueue.value = items.groupBy { historyItem -> historyItem.toDate() }
            }
        }
    }
}