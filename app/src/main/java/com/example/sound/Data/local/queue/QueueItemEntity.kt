package com.example.sound.Data.local.queue

import androidx.core.net.toUri
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


fun QueueItem.toEntity(): QueueItemEntity {
    return QueueItemEntity(
        id = this.id,
        songId = this.song.id,
        songUri = this.song.uri.toString(),
        position = position,
        title = this.song.title,
        artist = this.song.artist,
        duration = this.song.duration,
        album = this.song.album,
        genre = this.song.genre,
        artUri = this.song.art?.toString()
    )
}

fun QueueItemEntity.toDomain(): QueueItem {
    return QueueItem(
        song = Song(
            id = songId,
            title = title,
            artist = artist,
            duration = duration,
            uri = songUri,
            album = album,
            genre = genre,
            art = artUri
        ),
        id = id,
        position = position
    )
}

fun Song.toQueueItemEntity(position: Int): QueueItemEntity {
    return QueueItemEntity(
        id = 0,
        songId = this.id,
        songUri = this.uri.toString(),
        position = position,
        title = this.title,
        artist = this.artist,
        duration = this.duration,
        album = this.album,
        genre = this.genre,
        artUri = this.art?.toString()
    )
}
