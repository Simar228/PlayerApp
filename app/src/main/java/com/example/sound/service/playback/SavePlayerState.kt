package com.example.sound.service.playback

import androidx.media3.common.MediaItem
import com.example.sound.Domain.repository.PlaybackTransitionRepository
import javax.inject.Inject

class SavePlayerState @Inject constructor(
    private val playbackTransitionRepository: PlaybackTransitionRepository
) {
    suspend operator fun invoke(mediaItem: MediaItem) {
        playbackTransitionRepository.saveTransition(
            song = mediaItem.toSong(),
            queueItemId = mediaItem.queueItemIdOrNull()
        )
    }
}