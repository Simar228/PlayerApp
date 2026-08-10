package com.example.sound.Data.local.playerstate

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sound.Data.local.DatabaseTableNames

@Dao
interface PlayerStateDao {

    @Query("SELECT * FROM ${DatabaseTableNames.PLAYER_STATE} WHERE id = 0")
    suspend fun getPlayerState(): PlayerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerState(state: PlayerStateEntity)

    @Query("DELETE FROM ${DatabaseTableNames.PLAYER_STATE}")
    suspend fun clearPlayerState()

}
