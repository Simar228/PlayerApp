package com.example.sound.Presentation.songPage


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.playerUi.PlayerUIEvent
import com.example.sound.Presentation.playerUi.viewModel.PlayerViewModel
import com.example.sound.Presentation.songPage.components.PlaybackControls
import com.example.sound.Presentation.songPage.components.PlayerBottomActions
import com.example.sound.Presentation.songPage.components.PlayerTime
import com.example.sound.Presentation.songPage.components.PlayerTopBar
import com.example.sound.Presentation.songPage.components.SongArtwork
import com.example.sound.Presentation.songPage.components.SongInformation
import com.example.sound.ui.theme.SoundTheme


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
    onEditClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val playerUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val durationMs = playerUiState.duration
    val positionMs = playerUiState.currentPosition
    val song = playerUiState.currentSong
    val isPlaying = playerUiState.isPlaying

    SongPageView(
        song = song,
        positionMs = positionMs,
        durationMs = durationMs,
        isPlaying = isPlaying,
        isFavorite = isFavorite,
        shuffleEnabled = shuffleEnabled,
        repeatEnabled = repeatEnabled,
        onCloseClick = onCloseClick,
        onMenuClick = onMenuClick,
        onFavoriteClick = onFavoriteClick,
        onShuffleClick = onShuffleClick,
        onRepeatClick = onRepeatClick,
        onQueueClick = onQueueClick,
        onEditClick = onEditClick,
        onPlayerEvent = viewModel::sendEvent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongPageView(
    song: Song?,
    positionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    isFavorite: Boolean,
    shuffleEnabled: Boolean,
    repeatEnabled: Boolean,
    onCloseClick: () -> Unit,
    onMenuClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onQueueClick: () -> Unit,
    onEditClick: (String) -> Unit,
    onPlayerEvent: (PlayerUIEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSliderTouch by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val durationIsKnown = durationMs != C.TIME_UNSET && durationMs > 0L
    val safeDuration = if (durationIsKnown) durationMs else 1L

    LaunchedEffect(song?.id) {
        isSliderTouch = false
        sliderPosition = 0f
    }

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

                SongArtwork(
                    artwork = song.art?.toUri(),
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
                        onPlayerEvent(PlayerUIEvent.SeekTo(targetPosition))
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
                    isPlaying = isPlaying,
                    onPlayPauseClick = {
                        onPlayerEvent(
                            if (isPlaying) PlayerUIEvent.Pause else PlayerUIEvent.Play
                        )
                    },
                    onPreviousClick = { onPlayerEvent(PlayerUIEvent.PreviousSong) },
                    onNextClick = { onPlayerEvent(PlayerUIEvent.NextSong) },
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
                    onEditClick = { onEditClick(song.id) }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
@Preview
fun PreviewSongPage() {
    SoundTheme(darkTheme = true) {
        SongPageView(
            song = Song(
                id = "preview-song",
                title = "Preview Song",
                artist = "Preview Artist",
                duration = 215_000L,
                uri = "EMPTY",
                album = "Preview Album",
                genre = "Rock",
            ),
            positionMs = 72_000L,
            durationMs = 215_000L,
            isPlaying = true,
            isFavorite = true,
            shuffleEnabled = true,
            repeatEnabled = false,
            onCloseClick = {},
            onMenuClick = {},
            onFavoriteClick = {},
            onShuffleClick = {},
            onRepeatClick = {},
            onQueueClick = {},
            onEditClick = {},
            onPlayerEvent = {},
        )
    }
}
