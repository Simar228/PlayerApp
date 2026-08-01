package com.example.sound.service.playback

import android.net.Uri
import androidx.media3.common.Player
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.Domain.repository.PlayerStateRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class SavePlayerState @AssistedInject constructor(
    @Assisted val player: Player,
    val playerStateRepository: PlayerStateRepository,
    val playerQueueRepository: PlayerQueueRepository
) {
    suspend operator fun invoke() {
        val mediaItem = player.currentMediaItem ?: return
        val metadata = mediaItem.mediaMetadata
        val newSong = Song(
            id = mediaItem.mediaId,
            title = metadata.title?.toString().orEmpty(),
            artist = metadata.artist?.toString(),
            duration = metadata.durationMs ?: 0L,
            uri = mediaItem.localConfiguration?.uri ?: Uri.EMPTY,
            album = metadata.albumTitle?.toString(),
            genre = metadata.genre?.toString(),
            art = metadata.artworkUri,
        )
        playerStateRepository.setPlayerState(
            PlayerState(
                currentSong = newSong
            )
        )
        playerQueueRepository.deleteFirstSong(newSong)
    }

    @AssistedFactory
    interface Factory {
        fun create(player: Player): SavePlayerState
    }
}