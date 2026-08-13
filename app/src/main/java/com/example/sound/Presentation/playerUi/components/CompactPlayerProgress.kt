package com.example.sound.Presentation.playerUi.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.C

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactPlayerProgress(
    songId: String,
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSliderTouch by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    val durationIsKnown = duration != C.TIME_UNSET && duration > 0L
    val safeDuration = if (durationIsKnown) duration else 1L
    val safePosition = currentPosition.coerceIn(
        minimumValue = 0L,
        maximumValue = safeDuration
    )
    val sliderValue = if (isSliderTouch) {
        sliderPosition
    } else {
        safePosition.toFloat()
    }.coerceIn(0f, safeDuration.toFloat())

    LaunchedEffect(songId) {
        isSliderTouch = false
        sliderPosition = 0f
    }

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
            onSeek(targetPosition)
            isSliderTouch = false
        },
        thumb = {},
        enabled = durationIsKnown,
        valueRange = 0f..safeDuration.toFloat(),
        modifier = modifier.fillMaxWidth(),
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
}

@Preview(showBackground = true)
@Composable
private fun PreviewCompactPlayerProgress() {
    CompactPlayerProgress(
        songId = "preview-song",
        currentPosition = 43_000L,
        duration = 100_000L,
        onSeek = {}
    )
}
