package com.example.sound.Data.local.Genre

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sound.Data.local.DatabaseTableNames

@Dao
interface GenreDao {
    @Query("SELECT * FROM ${DatabaseTableNames.GENRE} ORDER BY name ASC ")
    suspend fun getAllGenres(): List<GenreEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenres(genres: List<GenreEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenre(genre: GenreEntity)
}