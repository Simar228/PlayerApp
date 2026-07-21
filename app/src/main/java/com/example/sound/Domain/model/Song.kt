package com.example.sound.Domain.model

import android.net.Uri

data class Song(
    val id: String,
    val title: String?,
    val artist: String?,
    val duration: Long,
    val uri: Uri,
    val album: String?,
    val genre: String?,
    val art: Uri? = null
)

fun Song.toQueueItem(position: Int): QueueItem{
    return QueueItem(
        id = 0,
        song = this,
        position = position
    )
}
