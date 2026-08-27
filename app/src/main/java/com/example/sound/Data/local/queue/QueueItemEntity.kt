package com.example.sound.Data.local.queue

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song


@Entity(
    tableName = DatabaseTableNames.QUEUE_ITEMS,
    indices = [
        Index(
            value = ["position"],
            unique = true
        )
    ]
)
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val position: Int,
    @Embedded(prefix = "song_")
    val song: Song
)


fun QueueItem.toEntity(): QueueItemEntity {
    return QueueItemEntity(
        id = this.id,
        song = song,
        position = position,
    )
}

fun QueueItemEntity.toDomain(): QueueItem {
    return QueueItem(
        song = song,
        id = id,
        position = position
    )
}

fun Song.toQueueItemEntity(position: Int): QueueItemEntity {
    return QueueItemEntity(
        position = position,
        song = this

    )
}
