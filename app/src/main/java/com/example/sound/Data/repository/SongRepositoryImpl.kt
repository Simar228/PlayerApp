package com.example.sound.Data.repository

import android.provider.MediaStore
import com.example.sound.Data.provider.FakeSongProvider
import com.example.sound.Data.provider.MediaStoreSongProvider
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.SongRepository
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val provider: MediaStoreSongProvider
) : SongRepository
{
    override fun getSong(): List<Song> {
        return provider.loadSongs()
    }

}