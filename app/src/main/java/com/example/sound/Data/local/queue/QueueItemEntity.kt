package com.example.sound.Data.local.queue

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song


@Entity(tableName = "queue_items")
data class QueueItemEntity(
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

fun QueueItemEntity.toDomain(): QueueItem {
    return QueueItem(
        id = id,
        position = position,
        song = Song(
            id = songId,
            title = title,
            artist = artist,
            duration = duration,
            uri = songUri.toUri(),
            album = album,
            genre = genre,
            art = artUri?.let(Uri::parse)
        ),

    )
}

fun QueueItem.toEntity(): QueueItemEntity {
    return QueueItemEntity(
        id = id,
        songId = song.id,
        songUri = song.uri.toString(),
        position = position,
        title = song.title,
        artist = song.artist,
        duration = song.duration,
        album = song.album,
        genre = song.genre,
        artUri = song.art?.toString()
    )
}
