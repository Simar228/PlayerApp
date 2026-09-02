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
    val song: Song,
    val fromUser: Boolean,
)


fun QueueItem.toEntity(): QueueItemEntity {
    return QueueItemEntity(
        id = this.id,
        song = song,
        position = position,
        fromUser = fromUser,
    )
}

fun QueueItemEntity.toDomain(): QueueItem {
    return QueueItem(
        song = song,
        id = id,
        position = position,
        fromUser = fromUser
    )
}

fun Song.toQueueItemEntity(position: Int, fromUser: Boolean): QueueItemEntity {
    return QueueItemEntity(
        song = this,
        id = 0,
        position = position,
        fromUser = fromUser
    )
}
