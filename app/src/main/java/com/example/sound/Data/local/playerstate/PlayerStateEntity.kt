package com.example.sound.Data.local.playerstate

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "player_state")
data class PlayerStateEntity(
    @PrimaryKey
    val id: Int = 0,

    val currentQueueItemId: Long?,
    val positionMs: Long
)