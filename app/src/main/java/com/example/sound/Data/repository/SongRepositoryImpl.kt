package com.example.sound.Data.repository

import com.example.sound.Data.provider.MediaStoreSongProvider
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.EditSongRepository
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import com.example.sound.Domain.repository.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val provider: MediaStoreSongProvider,
    private val editSongRepository: EditSongRepository,
    private val playbackTransitionRepository: PlaybackTransitionRepository,
) : SongRepository {
    private val scope = CoroutineScope(SupervisorJob())
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    private var observeSongs: Job? = null

    override val songs = _songs.asStateFlow()

    override fun loadSongs(){
        if (observeSongs?.isActive == true) return
        observeSongs = scope.launch {
            val editSongs = editSongRepository.observeEditSongs()
            val originalSongs = provider.observeSongs()
            combine(
                originalSongs,
                editSongs,
            ) { original, edit ->

                val editById = edit.associateBy { it.id }

                original.map { originalSong ->
                    editById[originalSong.id] ?: originalSong
                }
            }.collect { songs ->
                _songs.value = songs
                playbackTransitionRepository.updateCurrentSongIfMatches(songs)
            }
        }

    }
}
