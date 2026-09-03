package com.example.sound.service.playback

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.sound.Domain.model.PlaybackQueueState


class PlaybackQueueSynchronizer(
    private val player: Player
) {
    fun synchronizePlayerQueue(state: PlaybackQueueState) {
        val desiredItems = state.queueItems.map { item -> item.toMediaItem() }

        if (desiredItems.isEmpty()) {
            if (player.mediaItemCount > 0) {
                player.clearMediaItems()
            }
            return
        }

        if (player.mediaItemCount == 0 || player.currentMediaItem == null) {
            setNewPlayerQueue(desiredItems)
            return
        }

        val desiredCurrentItem = desiredItems.first()
        val currentItem = player.currentMediaItem
        val isSameCurrentQueueItem =
            desiredCurrentItem.queueItemIdOrNull() == currentItem?.queueItemIdOrNull()

        if (!isSameCurrentQueueItem) {
            setNewPlayerQueue(desiredItems)
            return
        }

        val currentIndex = player.currentMediaItemIndex
        if (currentIndex == C.INDEX_UNSET) {
            return
        }

        if (currentIndex > 0) {
            player.removeMediaItems(0, currentIndex)
        }

        player.replaceMediaItems(
            1,
            player.mediaItemCount,
            desiredItems.drop(1),
        )

        val currentPlaylistItem = player.currentMediaItem
        if (
            currentPlaylistItem != null &&
            !currentPlaylistItem.hasSameMetadataAs(desiredCurrentItem) &&
            currentPlaylistItem.localConfiguration?.uri ==
            desiredCurrentItem.localConfiguration?.uri
        ) {
            player.replaceMediaItem(0, desiredCurrentItem)
        }
    }

    private fun setNewPlayerQueue(
        upcomingMediaItems: List<MediaItem>,
    ) {
        player.apply {
            repeatMode = Player.REPEAT_MODE_ALL
            shuffleModeEnabled = false
            setMediaItems(
                upcomingMediaItems,
                0,
                0
            )

            prepare()
            play()
        }
    }

    private fun MediaItem.hasSameMetadataAs(other: MediaItem): Boolean {
        val metadata = mediaMetadata
        val otherMetadata = other.mediaMetadata

        return mediaId == other.mediaId &&
                metadata.title?.toString() == otherMetadata.title?.toString() &&
                metadata.artist?.toString() == otherMetadata.artist?.toString() &&
                metadata.albumTitle?.toString() == otherMetadata.albumTitle?.toString() &&
                metadata.genre?.toString() == otherMetadata.genre?.toString() &&
                metadata.artworkUri == otherMetadata.artworkUri &&
                metadata.durationMs == otherMetadata.durationMs
    }
}
