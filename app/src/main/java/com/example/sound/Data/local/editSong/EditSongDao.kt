package com.example.sound.Data.local.editSong

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sound.Data.local.DatabaseTableNames
import kotlinx.coroutines.flow.Flow

@Dao
interface EditSongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEditSong(editSongItemEntity: EditSongItemEntity)

    @Query("SELECT * FROM ${DatabaseTableNames.EDIT_SONG}")
    fun observeEditSong(): Flow<List<EditSongItemEntity>>


}