package com.example.sound.Data.local.historyQueue

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.Song


@Entity(
    tableName = DatabaseTableNames.HISTORY_QUEUE_ITEMS,
    indices = [
        Index(
            value = ["position"],
            unique = true
        )
    ]
)
data class HistoryQueueItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val position: Int,
    @Embedded(prefix = "song_")
    val song: Song
)

fun Song.toHistoryQueueItemEntity(position: Int): HistoryQueueItemEntity {
    return HistoryQueueItemEntity(
        position = position,
        song = this
    )
}
