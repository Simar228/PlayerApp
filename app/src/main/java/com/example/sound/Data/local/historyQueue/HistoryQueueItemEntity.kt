package com.example.sound.Data.local.historyQueue

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.HistoryItem
import com.example.sound.Domain.model.Song
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


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
    val playedAt: Long,
    @Embedded(prefix = "song_")
    val song: Song
)

fun Song.toHistoryQueueItemEntity(position: Int, playedAt: Long): HistoryQueueItemEntity {
    return HistoryQueueItemEntity(
        playedAt = playedAt,
        position = position,
        song = this
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun HistoryItem.toDate(): String = Instant.ofEpochMilli(this.playedAt)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("dd.MM"))

