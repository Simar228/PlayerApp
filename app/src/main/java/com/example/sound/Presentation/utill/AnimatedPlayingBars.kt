package com.example.sound.Presentation.utill

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

@Composable
fun AnimatedPlayingBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    val barHeights: List<Float>
    if (isPlaying) {
        val transition = rememberInfiniteTransition(
            label = "playingBars"
        )

        val firstBar by transition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 420,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "firstBar"
        )

        val secondBar by transition.animateFloat(
            initialValue = 0.85f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 560,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "secondBar"
        )

        val thirdBar by transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 480,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "thirdBar"
        )

        barHeights = listOf(
            firstBar,
            secondBar,
            thirdBar
        )
    } else {
        barHeights = listOf(
            0.3f,
            0.3f,
            0.3f
        )
    }

    Canvas(
        modifier = modifier
    ) {
        drawPlayingBars(
            heights = barHeights,
            color = color
        )
    }
}

private fun DrawScope.drawPlayingBars(
    heights: List<Float>,
    color: Color
) {
    val barWidth = size.width * 0.18f
    val space = size.width * 0.12f

    val totalWidth =
        barWidth * heights.size +
                space * (heights.size - 1)

    val startX = (size.width - totalWidth) / 2f

    heights.forEachIndexed { index, heightFraction ->
        val barHeight = size.height * heightFraction

        val x = startX + index * (barWidth + space)
        val y = size.height - barHeight

        drawRoundRect(
            color = color,
            topLeft = Offset(
                x = x,
                y = y
            ),
            size = Size(
                width = barWidth,
                height = barHeight
            ),
            cornerRadius = CornerRadius(
                x = barWidth / 2f,
                y = barWidth / 2f
            )
        )
    }
}