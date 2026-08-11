package com.example.sound.Data.local.defualtQueue

import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sound.Data.local.DatabaseTableNames
import com.example.sound.Domain.model.Song

@Entity(
    tableName = DatabaseTableNames.DEFAULT_QUEUE_ITEMS,
    indices = [
        Index(
            value = ["position"],
            unique = true
        )
    ]
)
class DefaultQueueItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

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

fun DefaultQueueItemEntity.toSong(): Song {
    return Song(
        id = songId,
        title = title,
        artist = artist,
        duration = duration,
        uri = songUri,
        album = album,
        genre = genre,
        art = artUri
    )
}


fun Song.toDefaultQueueEntity(position: Int): DefaultQueueItemEntity {
    return DefaultQueueItemEntity(
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
