package com.example.sound.service.playback

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.sound.Domain.model.PlaybackQueueState


class PlaybackQueueSynchronizer(
    private val player: Player
) {
    fun synchronizePlayerQueue(state: PlaybackQueueState) {
        val player = player


        val upcomingMediaItems = state.playerQueueSongs.map { it.toMediaItem() }

        //запуск первый раз
        if (
            player.mediaItemCount == 0
        ) {
            Log.d("IF","1")
            setNewPlayerQueue(
                upcomingMediaItems = upcomingMediaItems,
            )
            return
        } else if (upcomingMediaItems.firstOrNull()?.mediaId != player.currentMediaItem?.mediaId) {
            player.replaceMediaItems(
                0,
                upcomingMediaItems.size,
                upcomingMediaItems
            )
            Log.d("IF","2")
        } else {
            player.replaceMediaItems(
                1,
                upcomingMediaItems.size,
                upcomingMediaItems.drop(1)
            )
            Log.d("IF","3")

        }

    }

    private fun setNewPlayerQueue(
        upcomingMediaItems: List<MediaItem>,
    ) {
        val player = player

        val mediaItems = upcomingMediaItems

        player.apply {
            repeatMode = Player.REPEAT_MODE_ALL
            shuffleModeEnabled = false
            setMediaItems(
                mediaItems,
                0,
                0
            )

            prepare()
            play()
        }
    }
}