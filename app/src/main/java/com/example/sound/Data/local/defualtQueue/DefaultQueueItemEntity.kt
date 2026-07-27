package com.example.sound.Data.local.defualtQueue

import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sound.Domain.model.DefaultQueueItem
import com.example.sound.Domain.model.Song

@Entity(tableName = "defaultQueue_items")
class DefaultQueueItemEntity (
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

fun DefaultQueueItemEntity.toDomain(): DefaultQueueItem{
    return DefaultQueueItem(
        id = id,
        song = Song(
            id = songId,
            title = title,
            artist = artist,
            duration = duration,
            uri = songUri.toUri(),
            album = album,
            genre = genre,
            art = artUri?.toUri()
        )
    )
}

fun DefaultQueueItem.toSong(): Song{
    return Song(
        id = song.id,
        title = song.title,
        artist = song.artist,
        duration = song.duration,
        uri = song.uri,
        album = song.album,
        genre = song.genre,
        art = song.art
    )
}

fun DefaultQueueItem.toEntity(position: Int): DefaultQueueItemEntity{
    return DefaultQueueItemEntity(
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
