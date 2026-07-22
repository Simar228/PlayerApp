package com.example.sound.Data.local.playerstate

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sound.Data.local.queue.QueueItemEntity
import com.example.sound.Domain.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStateDao {

    @Query("SELECT * FROM player_state  WHERE id = 0")
    fun observePlayerState(): Flow<PlayerStateEntity>
    @Query("SELECT * FROM player_state WHERE id = 0")
    suspend fun getPlayerState(): PlayerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerState(state: PlayerStateEntity)

    @Query("DELETE FROM player_state")
    suspend fun clearPlayerState()

}