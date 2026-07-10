package com.example.sound.service
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
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



}