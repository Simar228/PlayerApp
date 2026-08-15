package com.example.sound.Presentation.playerUi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.playerUi.components.CompactPlayerArtwork
import com.example.sound.Presentation.playerUi.components.CompactPlayerControls
import com.example.sound.Presentation.playerUi.components.CompactPlayerProgress
import com.example.sound.Presentation.playerUi.viewModel.PlayerViewModel


@Composable
fun PlayerUI(
    viewModel: PlayerViewModel,
    onClick: () -> Unit,
) {
    val playerUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val duration = playerUiState.duration
    val currentPosition = playerUiState.currentPosition
    val song = playerUiState.currentSong
    val isPlaying = playerUiState.isPlaying
    PlayerContent(
        song = song,
        duration = duration,
        currentPosition = currentPosition,
        isPlaying = isPlaying,
        onClick = onClick,
        onEvent = viewModel::sendEvent,
    )



}
@Composable
private fun PlayerContent(
    song: Song?,
    duration: Long,
    currentPosition: Long,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onEvent: (PlayerUIEvent) -> Unit,
) {

    song?.let {song ->
        Box(
            modifier = Modifier
                .clickable {
                    onClick()
                }
                .fillMaxWidth()
                .height(100.dp)
                .background(color = MaterialTheme.colorScheme.secondary.copy(alpha = 1f)),
            contentAlignment = Alignment.CenterStart,
        ) {

            CompactPlayerProgress(
                songId = song.id,
                currentPosition = currentPosition,
                duration = duration,
                onSeek = { positionMs ->
                    onEvent(PlayerUIEvent.SeekTo(positionMs))
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-22).dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                    .height(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactPlayerArtwork(
                    artwork = song.art,
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(70.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))


                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title ?: "Нету названия",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = song.artist ?: "Нету исполнителя",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    contentAlignment = Alignment.CenterEnd
                ) {
                    CompactPlayerControls(
                        isPlaying = isPlaying,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewPlayerUI() {
    PlayerContent(
        song = Song(
            id = "1",
            title = "Пока-Пока",
            artist = "CUPSIZE",
            duration = 90000,
            uri = "EMPTY",
            album = "кажется, в аду прикольно, но меня выгнали б утром",
            genre = "Инди-Рок",
            art = null
        ),
        duration = 100000,
        currentPosition = 43000,
        isPlaying = true,
        onClick = {},
        onEvent = {},
    )
}
