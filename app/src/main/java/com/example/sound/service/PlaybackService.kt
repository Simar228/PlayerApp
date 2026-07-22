package com.example.sound.service

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.sound.Domain.model.PlayerState
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.DefaultQueueRepository
import com.example.sound.Domain.repository.PlayerQueueRepository
import com.example.sound.Domain.repository.PlayerStateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer

    @Inject
    lateinit var playerQueueRepository: PlayerQueueRepository

    @Inject
    lateinit var defaultQueueRepository: DefaultQueueRepository

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
                addListener(playerListener)
            }
        mediaSession = MediaSession.Builder(this, player)
            .build()
        buildQueue()
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

    private fun buildQueue() {
        combine(
            playerStateRepository.observePlayerState(),
            playerQueueRepository.observeQueue(),
            defaultQueueRepository.observeQueue(),
        ) { currentSong, queueSongs, defaultQueueSongs ->
            Log.d(TAG, currentSong.toString())
            PlaybackQueueState(
                currentSong = currentSong,
                queueSongs = queueSongs,
                defaultQueueSongs = defaultQueueSongs,
            )
        }
            .distinctUntilChanged()
            .onEach(::synchronizePlayerQueue)
            .catch { error ->
                Log.e(TAG, "Queue observation error", error)
            }
            .launchIn(serviceScope)
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

        serviceScope.launch(Dispatchers.IO) {
            val queue = playerQueueRepository.observeQueue().first()
            val defaultQueue = queue.isEmpty()


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
                    defaultQueue = defaultQueue
                )
            )
        }
    }

    private fun synchronizePlayerQueue(state: PlaybackQueueState) {
        val player = player
        val currentSong = state.currentSong ?: return
        val upcomingMediaItems = buildList {
            // Явная очередь воспроизводится первой.
            addAll(
                state.queueSongs
                    .filterNot { song ->
                        song.id == currentSong.id
                    }
                    .map { song ->
                        song.toMediaItem(isDefaultQueue = false)
                    }
            )

            // Затем основной повторяемый плейлист.
            addAll(
                state.defaultQueueSongs
                    .filterNot { song ->
                        song.id == currentSong.id
                    }
                    .map { song ->
                        song.toMediaItem(isDefaultQueue = true)
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
        }
        Log.d(TAG, "// Песня не поменялась — обновляем только элементы вокруг неё.")
        // Песня не поменялась — обновляем только элементы вокруг неё.
        replaceUpcomingItems(upcomingMediaItems)
    }

    private fun setNewPlayerQueue(
        currentSong: Song,
        upcomingMediaItems: List<MediaItem>,
    ) {
        val player = player

        val mediaItems = buildList {
            add(currentSong.toMediaItem(isDefaultQueue = false))
            addAll(upcomingMediaItems)
        }

        player.apply {
            repeatMode = Player.REPEAT_MODE_OFF
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

}

private const val EXTRA_DEFAULT_QUEUE = "defaultQueue"
private fun Song.toMediaItem(isDefaultQueue: Boolean): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(art)
                .setDurationMs(duration)
                .setGenre(genre)
                .setAlbumTitle(album)
                .setExtras(
                    Bundle().apply {
                        putBoolean(EXTRA_DEFAULT_QUEUE, isDefaultQueue)
                    }
                )
                .build()
        )
        .build()
}

data class PlaybackQueueState(
    val currentSong: Song?,
    val queueSongs: List<Song>,
    val defaultQueueSongs: List<Song>,
)

const val TAG = "PlaybackService"