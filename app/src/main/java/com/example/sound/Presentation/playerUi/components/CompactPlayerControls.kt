package com.example.sound.Presentation.playerUi.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sound.Presentation.playerUi.PlayerUIEvent
import com.example.sound.ui.theme.SoundTheme

@Composable
fun CompactPlayerControls(
    isPlaying: Boolean,
    onEvent: (PlayerUIEvent) -> Unit,
) {
    Row {
        IconButton(
            onClick = {
                onEvent(PlayerUIEvent.PreviousSong)
            }
        ) {
            Icon(
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onSecondary,
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = null
            )
        }

        IconButton(
            onClick = {
                if (isPlaying) {
                    onEvent(PlayerUIEvent.Pause)
                } else {
                    onEvent(PlayerUIEvent.Play)
                }
            }
        ) {
            Icon(
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onSecondary,
                imageVector = if (isPlaying) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = null,
            )
        }

        IconButton(
            onClick = {
                onEvent(PlayerUIEvent.NextSong)
            }
        ) {
            Icon(
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.onSecondary,
                imageVector = Icons.Filled.SkipNext,
                contentDescription = null
            )
        }
    }
}

@Preview(name = "Playing")
@Composable
private fun PreviewCompactPlayerControlsPlaying() {
    SoundTheme(darkTheme = true) {
        CompactPlayerControls(
            isPlaying = true,
            onEvent = {}
        )
    }
}

@Preview(name = "Paused")
@Composable
private fun PreviewCompactPlayerControlsPaused() {
    SoundTheme(darkTheme = true) {
        CompactPlayerControls(
            isPlaying = false,
            onEvent = {}
        )
    }
}
