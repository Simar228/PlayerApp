package com.example.sound.Data.local.historyQueue

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames


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

    val songId: String,
    val songUri: String,
    val position: Int,
    val title: String?,
    val artist: String?,
    val duration: Long,
    val album: String?,
    val genre: String?,
    val artUri: String?
)
