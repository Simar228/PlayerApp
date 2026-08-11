package com.example.sound.Data.repository

import com.example.sound.Data.provider.MediaStoreSongProvider
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val provider: MediaStoreSongProvider
) : SongRepository {
    override fun observeSongs(): Flow<List<Song>> {
        return provider.observeSongs()
    }

}
