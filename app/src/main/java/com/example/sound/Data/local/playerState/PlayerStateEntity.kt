package com.example.sound.Data.local.playerState

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song


@Entity(tableName = DatabaseTableNames.PLAYER_STATE)
data class PlayerStateEntity(
    @PrimaryKey
    val id: Int = 0,

    @Embedded(prefix = "song_")
    val currentSong: Song

)

fun Song.toPlayerStateEntity(): PlayerStateEntity {
    return PlayerStateEntity(
        currentSong = this
    )
}

fun PlayerStateEntity.toDomain(): PlayerState {
    return PlayerState(
        currentSong
    )
}