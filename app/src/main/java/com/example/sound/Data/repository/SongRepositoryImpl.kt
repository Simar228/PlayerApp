package com.example.sound.Data.repository

import android.util.Log
import com.example.sound.Data.provider.MediaStoreSongProvider
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val provider: MediaStoreSongProvider,
) : SongRepository {
    private val scope = CoroutineScope(SupervisorJob())
    private val _songs = MutableStateFlow<List<Song>>(emptyList())

    override val songs = _songs.asStateFlow()

    init {
        Log.d("SRI", "INIT")
        scope.launch {
            provider.observeSongs().collect(_songs)
        }
    }
}
