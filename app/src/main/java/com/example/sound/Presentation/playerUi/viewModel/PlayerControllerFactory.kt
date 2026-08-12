package com.example.sound.Presentation.playerUi.viewModel

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.sound.service.PlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PlayerControllerFactory @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) {
    fun create(
        onControllerReady: () -> Unit
    ): PlayerController {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        val controllerFuture = MediaController.Builder(
            context,
            sessionToken
        ).buildAsync()

        return PlayerController(
            controllerFuture = controllerFuture,
            controllerListenerExecutor =
                ContextCompat.getMainExecutor(context),
            onControllerReady = onControllerReady
        )
    }
}