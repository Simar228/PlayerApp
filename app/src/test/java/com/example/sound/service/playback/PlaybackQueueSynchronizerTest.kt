package com.example.sound.service.playback

import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.sound.Domain.model.PlaybackQueueState
import com.example.sound.Domain.model.QueueItem
import com.example.sound.testing.createTestSong
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PlaybackQueueSynchronizerTest {

    private lateinit var player: Player
    private lateinit var synchronizer: PlaybackQueueSynchronizer

    @Before
    fun setUp() {
        player = mock()
        synchronizer = PlaybackQueueSynchronizer(player)
    }

    @Test
    fun `does nothing when current song is null`() {
        val state = PlaybackQueueState(
            currentSong = null,
            queueItems = emptyList(),
            defaultQueueSongs = emptyList(),
        )

        synchronizer.synchronizePlayerQueue(state)

        verify(player, never()).setMediaItems(
            any<List<MediaItem>>(),
            any(),
            any(),
        )
        verify(player, never()).play()
    }

    @Test
    fun `sets new queue on first launch`() {
        val currentSong = createTestSong(id = "1")
        val nextSong = createTestSong(id = "2")

        whenever(player.mediaItemCount).thenReturn(0)
        whenever(player.currentMediaItem).thenReturn(null)

        val state = PlaybackQueueState(
            currentSong = currentSong,
            queueItems = emptyList(),
            defaultQueueSongs = listOf(
                currentSong,
                nextSong,
            ),
        )

        synchronizer.synchronizePlayerQueue(state)

        val captor = argumentCaptor<List<MediaItem>>()

        verify(player).setMediaItems(
            captor.capture(),
            eq(0),
            eq(0L),
        )

        val result = captor.firstValue

        assertThat(result.map { it.mediaId })
            .containsExactly(
                currentSong.id,
                nextSong.id,
            )
            .inOrder()

        verify(player).prepare()
        verify(player).play()
    }

    @Test
    fun `sets new queue when user selects another song`() {
        val oldSong = createTestSong(id = "old")
        val newSong = createTestSong(id = "new")

        whenever(player.mediaItemCount).thenReturn(2)
        whenever(player.currentMediaItem).thenReturn(
            oldSong.toMediaItem()
        )

        val state = PlaybackQueueState(
            currentSong = newSong,
            queueItems = emptyList(),
            defaultQueueSongs = listOf(newSong),
        )

        synchronizer.synchronizePlayerQueue(state)

        verify(player).setMediaItems(
            any<List<MediaItem>>(),
            eq(0),
            eq(0L),
        )

        verify(player).prepare()
        verify(player).play()
    }

    @Test
    fun `replaces current item when same song was edited`() {
        val currentSong = createTestSong(
            id = "1",
            title = "New title"
        )

        whenever(player.mediaItemCount).thenReturn(1)
        whenever(player.currentMediaItem).thenReturn(
            createTestSong(
                id = "1",
                title = "Old title"
            ).toMediaItem()
        )
        whenever(player.currentMediaItemIndex).thenReturn(0)

        val state = PlaybackQueueState(
            currentSong = currentSong,
            queueItems = emptyList(),
            defaultQueueSongs = emptyList(),
        )

        synchronizer.synchronizePlayerQueue(state)

        val captor = argumentCaptor<MediaItem>()

        verify(player).replaceMediaItem(
            eq(0),
            captor.capture()
        )

        assertThat(captor.firstValue.mediaId)
            .isEqualTo("1")

        assertThat(captor.firstValue.mediaMetadata.title?.toString())
            .isEqualTo("New title")
    }

    @Test
    fun `explicit queue comes before default queue`() {
        val currentSong = createTestSong(id = "1")
        val queuedSong = createTestSong(id = "2")
        val defaultSong = createTestSong(id = "3")

        whenever(player.mediaItemCount).thenReturn(0)
        whenever(player.currentMediaItem).thenReturn(null)

        val state = PlaybackQueueState(
            currentSong = currentSong,
            queueItems = listOf(
                QueueItem(
                    id = 1L,
                    song = queuedSong,
                    position = 0,
                )
            ),
            defaultQueueSongs = listOf(
                currentSong,
                defaultSong,
            ),
        )

        synchronizer.synchronizePlayerQueue(state)

        val captor = argumentCaptor<List<MediaItem>>()

        verify(player).setMediaItems(
            captor.capture(),
            eq(0),
            eq(0L),
        )

        assertThat(captor.firstValue.map { it.mediaId })
            .containsExactly(
                currentSong.id,
                queuedSong.id,
                defaultSong.id,
            )
            .inOrder()
    }

    @Test
    fun `removes played items and replaces upcoming items`() {
        val currentSong = createTestSong(id = "current")
        val nextSong = createTestSong(id = "next")

        whenever(player.mediaItemCount)
            .thenReturn(4)
            .thenReturn(2)

        whenever(player.currentMediaItem).thenReturn(
            currentSong.toMediaItem()
        )

        whenever(player.currentMediaItemIndex).thenReturn(2)

        val state = PlaybackQueueState(
            currentSong = currentSong,
            queueItems = emptyList(),
            defaultQueueSongs = listOf(
                currentSong,
                nextSong,
            ),
        )

        synchronizer.synchronizePlayerQueue(state)

        verify(player).removeMediaItems(
            0,
            2,
        )

        verify(player).addMediaItems(
            eq(1),
            any<List<MediaItem>>(),
        )
    }

    @Test
    fun `does not modify upcoming queue when current index is unset`() {
        val currentSong = createTestSong(id = "1")

        whenever(player.mediaItemCount).thenReturn(1)
        whenever(player.currentMediaItem).thenReturn(
            currentSong.toMediaItem()
        )
        whenever(player.currentMediaItemIndex)
            .thenReturn(C.INDEX_UNSET)

        val state = PlaybackQueueState(
            currentSong = currentSong,
            queueItems = emptyList(),
            defaultQueueSongs = emptyList(),
        )

        synchronizer.synchronizePlayerQueue(state)

        verify(player, never()).removeMediaItems(
            any(),
            any(),
        )

        verify(player, never()).addMediaItems(
            any(),
            any<List<MediaItem>>(),
        )
    }
}