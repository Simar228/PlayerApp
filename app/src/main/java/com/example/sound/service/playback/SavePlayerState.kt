package com.example.sound.service.playback

import androidx.media3.common.Player
import com.example.sound.Domain.model.PlayerState
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
        val newSong = player.currentMediaItem
            ?.toSong()
            ?: return
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