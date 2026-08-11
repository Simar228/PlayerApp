package com.example.sound.Presentation.playerUi

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.Presentation.playerUi.viewModel.PlayerViewModel
import com.google.common.util.concurrent.ListenableFuture
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.concurrent.Executor

class PlayerViewModelTest {

    @Test
    fun `controller plays latest song requested while connecting`() {
        // Given
        val repository = mock(PlayerQueueRepository::class.java)
        val controller = mock(MediaController::class.java)

        @Suppress("UNCHECKED_CAST")
        val controllerFuture = mock(ListenableFuture::class.java)
                as ListenableFuture<MediaController>

        var controllerReadyListener: Runnable? = null
        doAnswer { invocation ->
            controllerReadyListener = invocation.getArgument(0)
            null
        }.`when`(controllerFuture).addListener(
            any(Runnable::class.java),
            any(Executor::class.java)
        )
        `when`(controllerFuture.get()).thenReturn(controller)

        val viewModel = PlayerViewModel(
            playerQueueRepository = repository,
            controllerFuture = controllerFuture,
            controllerListenerExecutor = Executor { command -> command.run() }
        )

        val songs = listOf(
            createSong(id = "A"),
            createSong(id = "B"),
            createSong(id = "C")
        )

        // When
        viewModel.sendSong(songs, songs[0])
        viewModel.sendSong(songs, songs[1])
        viewModel.sendSong(songs, songs[2])
        controllerReadyListener?.run()

        // Then
        @Suppress("UNCHECKED_CAST")
        val mediaItemsCaptor = ArgumentCaptor.forClass(List::class.java)
                as ArgumentCaptor<List<MediaItem>>

        verify(controller).setMediaItems(
            mediaItemsCaptor.capture(),
            eq(2),
            eq(0L)
        )
        verify(controller).prepare()
        verify(controller).play()

        val actualMediaIds = mediaItemsCaptor.value.map { mediaItem ->
            mediaItem.mediaId
        }
        val expectedMediaIds = listOf("A", "B", "C")

        assertEquals(expectedMediaIds, actualMediaIds)
        assertEquals(songs[2], viewModel.currentSong.value)
    }

    private fun createSong(id: String): Song {
        return Song(
            id = id,
            title = "Song $id",
            artist = "Artist",
            duration = 1_000L,
            uri = mock(Uri::class.java),
            album = null,
            genre = null,
            art = null
        )
    }
}
