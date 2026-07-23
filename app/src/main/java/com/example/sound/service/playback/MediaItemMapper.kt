package com.example.sound.service.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.sound.Domain.model.Song


fun Song.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(art)
                .setDurationMs(duration)
                .setGenre(genre)
                .setAlbumTitle(album)
                .build()
        )
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