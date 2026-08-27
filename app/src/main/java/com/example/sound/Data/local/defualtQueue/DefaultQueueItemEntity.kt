package com.example.sound.Data.local.defualtQueue

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.Song

@Entity(
    tableName = DatabaseTableNames.DEFAULT_QUEUE_ITEMS,
)
class DefaultQueueItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val position: Int,
    @Embedded(prefix = "song_")
    val song: Song
)

fun Song.toDefaultQueueEntity(position: Int): DefaultQueueItemEntity {
    return DefaultQueueItemEntity(
        song = this,
        position = position
    )
}
