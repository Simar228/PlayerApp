package com.example.sound.service.playback

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.sound.Domain.model.PlaybackQueueState
import com.example.sound.Domain.model.Song


class PlaybackQueueSynchronizer(
    private val player: Player
) {
    fun synchronizePlayerQueue(state: PlaybackQueueState) {
        val player = player
        val currentSong = state.currentSong ?: return
        val defaultQueueSongs = defaultQueueAfterCurrentSong(
            defaultQueue = state.defaultQueueSongs,
            currentSong = currentSong
        )


        val upcomingMediaItems = buildList {
            // Явная очередь воспроизводится первой.
            addAll(
                state.queueItems.map { queueItem ->
                    queueItem.toMediaItem()
                }
            )

            // Затем основной повторяемый плейлист.
            addAll(
                defaultQueueSongs.map { song ->
                    song.toMediaItem()
                }
            )
        }

        val playerCurrentSongId = player.currentMediaItem?.mediaId

        //запуск первый раз
        if (
            player.mediaItemCount == 0 ||
            playerCurrentSongId == null
        ) {
            Log.d(TAG, "Запуск в первый раз")
            setNewPlayerQueue(
                currentSong = currentSong,
                upcomingMediaItems = upcomingMediaItems,
            )
            return
        }

        //Пользователь выбрал новую песню
        if (playerCurrentSongId != currentSong.id) {

            Log.d(
                TAG, "// currentSong действительно поменялась:\n" +
                        "            // пользователь выбрал новую песню."
            )
            // currentSong действительно поменялась:
            // пользователь выбрал новую песню.
            setNewPlayerQueue(
                currentSong = currentSong,
                upcomingMediaItems = upcomingMediaItems,
            )
            return
        } else { //Пользователь изменил текущую песню
            val currentIndex = player.currentMediaItemIndex

            if (currentIndex == C.INDEX_UNSET) {
                Log.e("PlaybackService", "currentIndex = -1")
            }

            player.replaceMediaItem(
                currentIndex,
                currentSong.toMediaItem(),
            )
        }

        Log.d(TAG, "// Песня не поменялась — обновляем только элементы вокруг неё.")
        replaceUpcomingItems(upcomingMediaItems)

    }

    private fun setNewPlayerQueue(
        currentSong: Song,
        upcomingMediaItems: List<MediaItem>,
    ) {
        val player = player

        val mediaItems = buildList {
            add(currentSong.toMediaItem())
            addAll(upcomingMediaItems)
        }
        Log.d(TAG, "Очередь ${mediaItems.size}")

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

    private fun replaceUpcomingItems(
        upcomingMediaItems: List<MediaItem>,
    ) {
        val player = player

        val currentIndex = player.currentMediaItemIndex

        if (currentIndex == C.INDEX_UNSET) {
            return
        }
        /*
         * Удаляем уже проигранные элементы.
         * Текущий элемент не входит в диапазон.
         */
        if (currentIndex > 0) {
            player.removeMediaItems(
                0,
                currentIndex,
            )
        }

        /*
         * После удаления предыдущих элементов текущая песня
         * находится на индексе 0.
         */
        if (player.mediaItemCount > 1) {
            player.removeMediaItems(
                1,
                player.mediaItemCount,
            )
        }
        if (upcomingMediaItems.isNotEmpty()) {
            player.addMediaItems(
                1,
                upcomingMediaItems,
            )
        }
    }

    private val TAG = "PlaybackQueueSynchronizer"
}

private fun defaultQueueAfterCurrentSong(
    defaultQueue: List<Song>,
    currentSong: Song,
): List<Song> {
    val currentIndex = defaultQueue.indexOfFirst { song ->
        song.id == currentSong.id
    }

    if (currentIndex == -1) {
        return defaultQueue
    }

    return defaultQueue.drop(currentIndex + 1) +
            defaultQueue.take(currentIndex)
}