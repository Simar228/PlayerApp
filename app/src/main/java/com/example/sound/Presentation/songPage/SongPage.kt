package com.example.sound.Presentation.songPage


import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import coil.compose.SubcomposeAsyncImage
import com.example.sound.Presentation.playerUi.PlayerUIEvent
import com.example.sound.Presentation.playerUi.PlayerViewModel




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongPage(
    viewModel: PlayerViewModel,
    isFavorite: Boolean,
    shuffleEnabled: Boolean,
    repeatEnabled: Boolean,
    onCloseClick: () -> Unit,
    onMenuClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onQueueClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSliderTouch by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val song by viewModel.currentSong.collectAsStateWithLifecycle()
    val positionMs by viewModel.currentPosition.collectAsStateWithLifecycle()
    val durationMs by viewModel.duration.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val durationIsKnown = durationMs != C.TIME_UNSET && durationMs > 0L
    val safeDuration = if (durationIsKnown) durationMs else 1L

    val safePosition =
        positionMs.coerceIn(
            minimumValue = 0L,
            maximumValue = safeDuration
        )
    val sliderValue = if (isSliderTouch) {
        sliderPosition
    } else {
        safePosition.toFloat()
    }.coerceIn(0f, safeDuration.toFloat())
    song?.let {song ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF071620),
                            Color(0xFF031017),
                            Color(0xFF07151C)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                PlayerTopBar(
                    onCloseClick = onCloseClick,
                    onMenuClick = onMenuClick
                )

                Spacer(modifier = Modifier.height(8.dp))

                Artwork(
                    artwork = song.art,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                SongInformation(
                    title = song.title ?: "Нет названия",
                    artist = song.artist ?: "Нет артиста",
                    isFavorite = isFavorite,
                    onFavoriteClick = onFavoriteClick
                )

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    valueRange = 0f..safeDuration.toFloat(),
                    value = sliderValue,
                    onValueChange = {
                        isSliderTouch = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        val targetPosition = sliderPosition
                            .coerceIn(0f, safeDuration.toFloat())
                            .toLong()
                        viewModel.sendEvent(PlayerUIEvent.SeekTo(targetPosition))
                        isSliderTouch = false
                    },
                    thumb = {},
                    enabled = durationIsKnown,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    ),
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

                PlayerTime(
                    currentPosition = positionMs,
                    duration = durationMs
                )

                Spacer(modifier = Modifier.height(14.dp))

                PlaybackControls(
                    viewModel = viewModel,
                    isPlaying = isPlaying,
                    onPreviousClick = { viewModel.sendEvent(PlayerUIEvent.PreviousSong) },
                    onNextClick = { viewModel.sendEvent(PlayerUIEvent.NextSong) },
                    onShuffleClick = onShuffleClick,
                    onRepeatClick = onRepeatClick
                )

                Spacer(modifier = Modifier.weight(1f))

                PlayerBottomActions(
                    shuffleEnabled = shuffleEnabled,
                    repeatEnabled = repeatEnabled,
                    onQueueClick = onQueueClick,
                    onRepeatClick = onRepeatClick,
                    onShuffleClick = onShuffleClick,
                    onEditClick = onEditClick
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PlayerTopBar(
    onCloseClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Свернуть плеер",
            )
        }

        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Дополнительные действия",
            )
        }
    }
}

@Composable
private fun Artwork(
    artwork: Uri?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF262329),
                        Color(0xFF080A0C)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (artwork != null) {
            SubcomposeAsyncImage(
                model = artwork,
                contentDescription = "Обложка композиции",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { SongPageArtworkPlaceholder() },
                error = { SongPageArtworkPlaceholder() }
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
private fun SongPageArtworkPlaceholder() {
    Icon(
        imageVector = Icons.Default.MusicNote,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.55f),
        modifier = Modifier.size(72.dp)
    )
}

@Composable
private fun SongInformation(
    title: String,
    artist: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 21.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = artist,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) {
                    Icons.Default.Favorite
                } else {
                    Icons.Default.FavoriteBorder
                },
                contentDescription = if (isFavorite) {
                    "Удалить из избранного"
                } else {
                    "Добавить в избранное"
                },
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun PlayerTime(
    currentPosition: Long,
    duration: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = currentPosition.toPlayerTime(),
            fontSize = 12.sp
        )

        Text(
            text = duration.toPlayerTime(),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PlaybackControls(
    viewModel: PlayerViewModel,
    isPlaying: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffleClick) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Перемешать",
            )
        }

        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Предыдущая композиция",
                modifier = Modifier.size(32.dp)
            )
        }

        FilledIconButton(
            onClick = {
                if (isPlaying) {
                    viewModel.sendEvent(PlayerUIEvent.Pause)
                } else {
                    viewModel.sendEvent(PlayerUIEvent.Play)
                }
            },
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = if (isPlaying) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = if (isPlaying) {
                    "Пауза"
                } else {
                    "Воспроизвести"
                },
                modifier = Modifier.size(34.dp)
            )
        }

        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Следующая композиция",
                modifier = Modifier.size(32.dp)
            )
        }

        IconButton(onClick = onRepeatClick) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "Повтор",
            )
        }
    }
}

@Composable
private fun PlayerBottomActions(
    shuffleEnabled: Boolean,
    repeatEnabled: Boolean,
    onQueueClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PlayerAction(
            icon = {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null
                )
            },
            title = "Очередь",
            onClick = onQueueClick
        )

        PlayerAction(
            icon = {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null
                )
            },
            title = "Повтор",
            stateText = if (repeatEnabled) "Вкл." else null,
            active = repeatEnabled,
            onClick = onRepeatClick
        )

        PlayerAction(
            icon = {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = null
                )
            },
            title = "Перемешивание",
            stateText = if (shuffleEnabled) "Вкл." else null,
            active = shuffleEnabled,
            onClick = onShuffleClick
        )

        PlayerAction(
            icon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            },
            title = "Редактировать",
            onClick = onEditClick
        )
    }
}

@Composable
private fun PlayerAction(
    icon: @Composable () -> Unit,
    title: String,
    stateText: String? = null,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(42.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (active) MaterialTheme.colorScheme.primary else Color.White
            )
        ) {
            icon()
        }

        Text(
            text = title,
            fontSize = 10.sp,
            maxLines = 1
        )

        stateText?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun Long.toPlayerTime(): String {
    val totalSeconds = (this / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L

    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Composable
@Preview
fun PreviewSongPage() {
    SongPage(
        viewModel = viewModel(),
        isFavorite = true,
        shuffleEnabled = true,
        repeatEnabled = true,
        onCloseClick = {},
        onMenuClick = {},
        onFavoriteClick = {},
        onShuffleClick = {},
        onRepeatClick = {},
        onQueueClick = {},
        onEditClick = {},
    )
}
