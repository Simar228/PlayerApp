package com.example.sound.Presentation.utill

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeRevealDelete(
    isActive: Boolean = true,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    actionWidth: Dp = 88.dp,
    content: @Composable () -> Unit
) {
    if (isActive) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()

        val actionWidthPx = with(density) {
            actionWidth.toPx()
        }

        var offsetX by remember {
            mutableFloatStateOf(0f)
        }

        var cardWidthPx by remember {
            mutableFloatStateOf(0f)
        }

        var animationJob by remember {
            mutableStateOf<Job?>(null)
        }

        var isDeleting by remember {
            mutableStateOf(false)
        }

        fun animateTo(
            targetOffset: Float,
            afterAnimation: (() -> Unit)? = null
        ) {
            animationJob?.cancel()

            animationJob = scope.launch {
                animate(
                    initialValue = offsetX,
                    targetValue = targetOffset,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { value, _ ->
                    offsetX = value
                }

                afterAnimation?.invoke()
            }
        }

        fun delete() {
            if (isDeleting) return

            isDeleting = true

            animateTo(
                targetOffset = -cardWidthPx,
                afterAnimation = onDelete
            )
        }

        val draggableState = rememberDraggableState { delta ->
            if (!isDeleting && cardWidthPx > 0f) {
                offsetX = (offsetX + delta).coerceIn(
                    minimumValue = -cardWidthPx,
                    maximumValue = 0f
                )
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(0.dp))
                .onSizeChanged {
                    cardWidthPx = it.width.toFloat()
                }
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.error)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(actionWidth)
                        .clickable(
                            enabled = !isDeleting,
                            onClick = ::delete
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Удалить",
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = offsetX.roundToInt(),
                            y = 0
                        )
                    }
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .draggable(
                        enabled = !isDeleting,
                        state = draggableState,
                        orientation = Orientation.Horizontal,
                        onDragStarted = {
                            animationJob?.cancel()
                        },
                        onDragStopped = { velocity ->
                            val deleteThreshold = cardWidthPx * 0.65f
                            val openThreshold = actionWidthPx * 0.35f

                            when {
                                offsetX <= -deleteThreshold -> {
                                    delete()
                                }

                                offsetX <= -openThreshold ||
                                    velocity < -700f -> {
                                    animateTo(-actionWidthPx)
                                }

                                else -> {
                                    animateTo(0f)
                                }
                            }
                        }
                    )
            ) {
                content()
            }
        }
    } else {
        content()
    }
}

@Preview(name = "Swipe enabled")
@Composable
private fun PreviewSwipeRevealDeleteEnabled() {
    SwipeRevealDelete(
        isActive = true,
        onDelete = {}
    ) {
        SwipeRevealDeletePreviewContent()
    }
}

@Preview(name = "Swipe disabled")
@Composable
private fun PreviewSwipeRevealDeleteDisabled() {
    SwipeRevealDelete(
        isActive = false,
        onDelete = {}
    ) {
        SwipeRevealDeletePreviewContent()
    }
}

@Composable
private fun SwipeRevealDeletePreviewContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Queue item")
    }
}
