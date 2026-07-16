package com.example.sound.Presentation.mainScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.mainScreen.components.SortButtonValue
import com.example.sound.Presentation.mainScreen.components.choose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class MainViewModel() : ViewModel() {


    private val _songsQueue = MutableStateFlow<List<Song>>(emptyList())
    val songsQueue = _songsQueue.asStateFlow()
    private val _currentDirectionOfSort = MutableStateFlow(
        mutableListOf(
            SortButtonValue(
                0,
                true,
                true,
            ),
            SortButtonValue(
                1,
                true,
                false
            ),
            SortButtonValue(
                2,
                true,
                false,

                ),
            SortButtonValue(
                3,
                true,
                false
            ),
        )
    )
    val currentDirectionOfSort = _currentDirectionOfSort.asStateFlow()

    fun setQueueSong(songs: List<Song>) {
        _songsQueue.value = songs
    }

    init {
        Log.d(TAG, "Init")
    }

    fun sortQueueSong(
        sortBy: MainScreenEvents
    ) {
        when (sortBy) {
            is MainScreenEvents.SortByTitle -> {
                _currentDirectionOfSort.value = _currentDirectionOfSort.value.choose(0)
                val isUp = _currentDirectionOfSort.value[0].isUp
                if (isUp) {
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.title == null }
                            .thenBy { it.title?.lowercase().orEmpty() }
                    )
                } else {
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.title == null }
                            .thenByDescending { it.title?.lowercase().orEmpty() }
                    )
                }
            }

            is MainScreenEvents.SortByArtist -> {
                _currentDirectionOfSort.value = _currentDirectionOfSort.value.choose(1)
                val isUp = _currentDirectionOfSort.value[1].isUp
                if (isUp) {
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.artist == null }
                            .thenBy { it.artist?.lowercase().orEmpty() }
                    )
                } else {
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.artist == null }
                            .thenByDescending { it.artist?.lowercase().orEmpty() }
                    )
                }
            }

            is MainScreenEvents.SortByAlbum -> {
                _currentDirectionOfSort.value = _currentDirectionOfSort.value.choose(2)
                val isUp = _currentDirectionOfSort.value[2].isUp
                if (isUp) {
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.album == null }
                            .thenBy { it.album?.lowercase().orEmpty() }
                    )
                } else {
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.album == null }
                            .thenByDescending { it.album?.lowercase().orEmpty() }
                    )
                }
            }

            is MainScreenEvents.SortByGenre -> {
                _currentDirectionOfSort.value = _currentDirectionOfSort.value.choose(3)
                val isUp = _currentDirectionOfSort.value[3].isUp
                if (isUp) {
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.genre == null }
                            .thenBy { it.genre?.lowercase().orEmpty() }
                    )
                } else {
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.genre == null }
                            .thenByDescending { it.genre?.lowercase().orEmpty() }
                    )
                }
            }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        super.onCleared()
    }

}

const val TAG = "MainViewModel"