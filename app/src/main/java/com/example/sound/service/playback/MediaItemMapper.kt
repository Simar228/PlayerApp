package com.example.sound.service.playback

import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song


fun Song.toMediaItem(isHistory: Boolean = false): MediaItem {
    return buildMediaItem(isHistory = isHistory)
}

fun QueueItem.toMediaItem(): MediaItem {
    return song.buildMediaItem(queueItemId = id, isHistory = false)
}

private fun Song.buildMediaItem(
    queueItemId: Long? = null,
    isHistory: Boolean,
): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setArtworkUri(art?.toUri())
        .setDurationMs(duration)
        .setGenre(genre)
        .setAlbumTitle(album)
        .apply {
            if (queueItemId != null) {
                setExtras(
                    Bundle().apply {
                        putLong(EXTRA_QUEUE_ITEM_ID, queueItemId)
                        putBoolean(IS_HISTORY, isHistory)
                    }
                )
            }
        }
        .build()

    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(metadata)
        .build()
}

fun MediaItem.toSong(): Song {
    val metadata = mediaMetadata

    return Song(
        id = mediaId,
        uri = localConfiguration?.uri?.toString().orEmpty(),
        title = metadata.title?.toString().orEmpty(),
        artist = metadata.artist?.toString(),
        duration = metadata.durationMs ?: 0L,
        album = metadata.albumTitle?.toString(),
        genre = metadata.genre?.toString(),
        art = metadata.artworkUri?.toString(),
    )
}

fun MediaItem.isHistory(): Boolean {
    val extras = mediaMetadata.extras ?: return false
    if (!extras.containsKey(EXTRA_QUEUE_ITEM_ID)) {
        return false
    }

    return extras.getBoolean(IS_HISTORY)
}

fun MediaItem.queueItemIdOrNull(): Long? {
    val extras = mediaMetadata.extras ?: return null

    if (!extras.containsKey(EXTRA_QUEUE_ITEM_ID)) {
        return null
    }

    return extras.getLong(EXTRA_QUEUE_ITEM_ID)
}


private const val IS_HISTORY =
    "com.example.sound.extra.IS_HISTORY"
private const val EXTRA_QUEUE_ITEM_ID =
    "com.example.sound.extra.QUEUE_ITEM_ID"
