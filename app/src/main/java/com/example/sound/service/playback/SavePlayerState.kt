package com.example.sound.service.playback

import androidx.media3.common.Player
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class SavePlayerState @AssistedInject constructor(
    @Assisted private val player: Player,
    private val playbackTransitionRepository: PlaybackTransitionRepository
) {
    suspend operator fun invoke() {
        val mediaItem = player.currentMediaItem ?: return
        playbackTransitionRepository.saveTransition(
            song = mediaItem.toSong(),
            queueItemId = mediaItem.queueItemIdOrNull()
        )
    }

    @AssistedFactory
    interface Factory {
        fun create(player: Player): SavePlayerState
    }
}