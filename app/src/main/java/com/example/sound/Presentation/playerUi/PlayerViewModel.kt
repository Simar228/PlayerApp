package com.example.sound.Presentation.playerUi

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.service.PlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerQueueRepository: PlayerQueueRepository,
    @ApplicationContext context: Context
) : ViewModel() {
    private var controller: MediaController? = null
    private val _isPlaying = MutableStateFlow(true)
    val isPlaying = _isPlaying.asStateFlow()
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()
    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()
    private val playerListener = object : Player.Listener {


        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int
        ) {
            _currentSong.value = mediaItem?.toSong()
            _currentPosition.value = 0L
            updateDuration()
        }


        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                updateDuration()
            }
            if (playbackState == Player.STATE_ENDED) {
                _currentPosition.value = _duration.value
            }

        }
    }

    init {

        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val controllerFuture = MediaController.Builder(context, sessionToken)
            .buildAsync()

        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
                controller?.addListener(playerListener)
            },
            ContextCompat.getMainExecutor(context)
        )
        viewModelScope.launch {
            while (true) {
                delay(100L)

                val mediaController = controller
                if (mediaController?.isPlaying == true) {
                    _currentPosition.value =
                        mediaController.currentPosition.coerceAtLeast(0L)
                }
            }
        }
    }

    private fun updateDuration() {
        _duration.value = controller?.duration ?: C.TIME_UNSET
    }

    private fun playSong(queueSongs: List<Song>, selectedSong: Song) {
        val mediaItems = queueSongs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(song.art)
                        .build()
                )
                .build()
        }
        val selectedIndex =
            queueSongs.indexOfFirst { it.uri == selectedSong.uri }.takeIf { it >= 0 } ?: 0
        controller?.apply {
            setMediaItems(
                mediaItems,
                selectedIndex,
                0L
            )
            prepare()
            play()
        }
    }


    fun sendSong(queueSongs: List<Song>, song: Song) {
        _currentSong.value = song
        playSong(queueSongs, song)
    }

    fun sendEvent(event: PlayerUIEvent) {
        when (event) {
            is PlayerUIEvent.Play -> {
                controller?.play()
            }

            is PlayerUIEvent.Pause -> {
                controller?.pause()
            }

            is PlayerUIEvent.SeekTo -> {
                _currentPosition.value = event.positionMs
                controller?.seekTo(event.positionMs)
            }

            is PlayerUIEvent.NextSong -> {
                controller?.seekToNextMediaItem()
                _currentSong.value = controller?.currentMediaItem?.toSong()
            }

            is PlayerUIEvent.PreviousSong -> {
                controller?.seekToPreviousMediaItem()
                _currentSong.value = controller?.currentMediaItem?.toSong()
            }
        }
    }
}

private fun MediaItem.toSong(): Song{
    return Song(
        id = mediaId.toLong(),
        title = mediaMetadata.title?.toString().orEmpty(),
        artist = mediaMetadata.artist?.toString().orEmpty(),
        duration = 0L,
        uri = localConfiguration?.uri
            ?: Uri.EMPTY,
        album = mediaMetadata.albumTitle?.toString().orEmpty(),
        genre = mediaMetadata.genre?.toString().orEmpty(),
        art = mediaMetadata.artworkUri
    )

}
