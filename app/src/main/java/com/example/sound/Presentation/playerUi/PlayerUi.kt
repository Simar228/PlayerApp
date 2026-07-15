package com.example.sound.Presentation.playerUi

import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import coil.compose.SubcomposeAsyncImage
import com.example.sound.Domain.model.Song


@Composable
fun PlayerUI(
    viewModel: PlayerViewModel,
    onClick: () -> Unit,
) {
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val song by viewModel.currentSong.collectAsStateWithLifecycle(initialValue = null)
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    PlayerContent(
        song = song,
        duration = duration,
        currentPosition = currentPosition,
        isPlaying = isPlaying,
        onClick = onClick,
        onEvent = viewModel::sendEvent,
    )



}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerContent(
    song: Song?,
    duration: Long,
    currentPosition: Long,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onEvent: (PlayerUIEvent) -> Unit,
) {

    var isSliderTouch by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val durationIsKnown = duration != C.TIME_UNSET && duration > 0L
    val safeDuration = if (durationIsKnown) duration else 1L
    val safePosition =
        currentPosition.coerceIn(
            minimumValue = 0L,
            maximumValue = safeDuration
        )
    val sliderValue = if (isSliderTouch) {
        sliderPosition
    } else {
        safePosition.toFloat()
    }.coerceIn(0f, safeDuration.toFloat())
    LaunchedEffect(song?.id) {
        isSliderTouch = false
        sliderPosition = 0f
    }
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

            Slider(
                value = sliderValue,
                onValueChange = {
                    isSliderTouch = true
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    val targetPosition = sliderPosition
                        .coerceIn(0f, safeDuration.toFloat())
                        .toLong()
                    onEvent(PlayerUIEvent.SeekTo(targetPosition))
                    isSliderTouch = false
                },
                thumb = {},
                enabled = durationIsKnown,
                valueRange = 0f.. safeDuration.toFloat(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-22).dp,)
                    .fillMaxWidth(),
                track = { sliderState ->
                    SliderDefaults.Track(
                        thumbTrackGapSize = 0.dp,
                        drawStopIndicator = null,
                        sliderState = sliderState,
                        modifier = Modifier.height(4.dp),
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.secondary,
                            thumbColor = Color.Transparent
                        )
                    )
                },
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .height(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.art != null) {
                        SubcomposeAsyncImage(
                            model = song.art,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = { PlayerArtworkPlaceholder() },
                            error = { PlayerArtworkPlaceholder() }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                    Row() {
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
                                if (isPlaying){
                                    onEvent(PlayerUIEvent.Pause)
                                }
                                else {
                                    onEvent(PlayerUIEvent.Play)
                                }
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.onSecondary,
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
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
            }
        }
    }
}

@Composable
private fun PlayerArtworkPlaceholder() {
    Icon(
        imageVector = Icons.Rounded.MusicNote,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
@Preview
fun PreviewPlayerUI() {
    PlayerContent(
        song = Song(
            id = 1,
            title = "Пока-Пока",
            artist = "CUPSIZE",
            duration = 90000,
            uri = Uri.EMPTY,
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
