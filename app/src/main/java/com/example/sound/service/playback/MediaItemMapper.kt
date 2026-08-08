package com.example.sound.service.playback

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song


fun Song.toMediaItem(): MediaItem {
    return buildMediaItem()
}

fun QueueItem.toMediaItem(): MediaItem {
    return song.buildMediaItem(queueItemId = id)
}

private fun Song.buildMediaItem(
    queueItemId: Long? = null
): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setArtworkUri(art)
        .setDurationMs(duration)
        .setGenre(genre)
        .setAlbumTitle(album)
        .apply {
            if (queueItemId != null) {
                setExtras(
                    Bundle().apply {
                        putLong(EXTRA_QUEUE_ITEM_ID, queueItemId)
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
        uri = localConfiguration?.uri ?: Uri.EMPTY,
        title = metadata.title?.toString().orEmpty(),
        artist = metadata.artist?.toString(),
        duration = metadata.durationMs ?: 0L,
        album = metadata.albumTitle?.toString(),
        genre = metadata.genre?.toString(),
        art = metadata.artworkUri,
    )
}

fun MediaItem.queueItemIdOrNull(): Long? {
    val extras = mediaMetadata.extras ?: return null

    if (!extras.containsKey(EXTRA_QUEUE_ITEM_ID)) {
        return null
    }

    return extras.getLong(EXTRA_QUEUE_ITEM_ID)
}


private const val EXTRA_QUEUE_ITEM_ID =
    "com.example.sound.extra.QUEUE_ITEM_ID"