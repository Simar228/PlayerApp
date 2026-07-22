package com.example.sound.service
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.PlayerStateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer

    @Inject
    lateinit var playerStateRepository: PlayerStateRepository
    private var mediaSession: MediaSession? = null

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private val playerListener = object : Player.Listener {

        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int
        ) {
            // Пользователь переключил песню, в том числе
            // через системный плеер или Bluetooth.
            savePlayerState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            // Play или Pause.
            savePlayerState()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            // Перемотка или переход на другой элемент.
            savePlayerState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (
                playbackState == Player.STATE_ENDED ||
                playbackState == Player.STATE_IDLE
            ) {
                savePlayerState()
            }
        }

        override fun onTimelineChanged(
            timeline: Timeline,
            reason: Int
        ) {
            // Изменилась очередь плеера.
            savePlayerState()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                setHandleAudioBecomingNoisy(true)
            }
        mediaSession = MediaSession.Builder(this, player)
            .build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {

        mediaSession?.release()
        mediaSession = null
        player.release()

        super.onDestroy()
    }
    private fun savePlayerState() {
        val mediaItem = player.currentMediaItem ?: return

        val id = mediaItem.mediaId
        val uri = mediaItem.localConfiguration?.uri
        val metadata = mediaItem.mediaMetadata
        val title = metadata.title?.toString()
        val artist = metadata.artist?.toString()
        val artworkUri = metadata.artworkUri
        val duration = metadata.durationMs
        val genre = metadata.genre
        val album = metadata.albumTitle

        val defaultQueue = metadata.extras
            ?.getBoolean("defaultQueue", false)
            ?: false
        val positionMs = player.currentPosition

        serviceScope.launch(Dispatchers.IO) {
            playerStateRepository.setPlayerState(
                PlayerState(
                    currentSong = Song(
                        id = id,
                        title = title,
                        artist = artist,
                        duration = duration ?: 0,
                        uri = uri ?: Uri.EMPTY,
                        album = album.toString(),
                        genre = genre.toString(),
                        art = artworkUri
                    ),
                    positionMs = positionMs,
                    defaultQueue = defaultQueue
                )
            )
        }
    }


}