package com.example.sound.Data.local.editSong

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sound.Data.local.DatabaseTableNames
import kotlinx.coroutines.flow.Flow

@Dao
interface EditSongDao {


    @Transaction
    suspend fun setEditSong(id: String): EditSongItemEntity?{
        val editSongItem = getBySongId(id) ?: return null
        deleteBySongId(id)
        return editSongItem
    }

    @Query("SELECT * FROM ${DatabaseTableNames.EDIT_SONG} WHERE songId = :id LIMIT 1")
    suspend fun getBySongId(id: String): EditSongItemEntity?

    @Query("DELETE FROM ${DatabaseTableNames.EDIT_SONG} WHERE songId = :id")
    suspend fun deleteBySongId(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addEditSong(editSongItemEntity: EditSongItemEntity)

    @Query("SELECT * FROM ${DatabaseTableNames.EDIT_SONG}")
    fun observeEditSong(): Flow<List<EditSongItemEntity>>


}