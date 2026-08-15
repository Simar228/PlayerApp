package com.example.sound.Data.local.Genre

import androidx.room.Dao
import androidx.room.Query
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Data.local.playerState.PlayerStateEntity
import com.example.sound.Domain.model.Genre

@Dao
interface GenreDao {
    @Query("SELECT * FROM ${DatabaseTableNames.GENRE} ORDER BY name ASC ")
    suspend fun getAllGenres(): List<GenreEntity>
}