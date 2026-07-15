package com.example.sound.Presentation.mainScreen

import androidx.lifecycle.ViewModel
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class MainViewModel() : ViewModel()  {


    private val _songsQueue = MutableStateFlow<List<Song>>(emptyList())
    val songsQueue = _songsQueue.asStateFlow()



    fun setQueueSong(songs: List<Song>){
        _songsQueue.value = songs
    }

    fun sortQueueSong(
        sortBy: MainScreenEvents
    ){
        when(sortBy){
            is MainScreenEvents.SortByTitle -> {
                if(sortBy.isUp){
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.title == null }
                            .thenBy { it.title?.lowercase().orEmpty() }
                    )
                }else{
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.title == null }
                            .thenByDescending { it.title?.lowercase().orEmpty() }
                    )
                }
            }
            is MainScreenEvents.SortByAlbum -> {
                if(sortBy.isUp){
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.album == null }
                            .thenBy { it.album?.lowercase().orEmpty() }
                    )
                }else{
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.album == null }
                            .thenByDescending { it.album?.lowercase().orEmpty() }
                    )
                }
            }
            is MainScreenEvents.SortByArtist -> {
                if(sortBy.isUp){
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.artist == null }
                            .thenBy { it.artist?.lowercase().orEmpty() }
                    )
                }else{
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.artist == null }
                            .thenByDescending { it.artist?.lowercase().orEmpty() }
                    )
                }
            }
            is MainScreenEvents.SortByGenre -> {
                if(sortBy.isUp){
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.genre == null }
                            .thenBy { it.genre?.lowercase().orEmpty() }
                    )
                }else{
                    _songsQueue.value = _songsQueue.value.sortedWith(
                        compareBy<Song> { it.genre == null }
                            .thenByDescending { it.genre?.lowercase().orEmpty() }
                    )
                }
            }
        }
    }
}