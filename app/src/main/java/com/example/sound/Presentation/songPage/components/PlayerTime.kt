package com.example.sound.Presentation.songPage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun PlayerTime(
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

private fun Long.toPlayerTime(): String {
    val totalSeconds = (this / 1_000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L

    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Preview
@Composable
private fun PreviewPlayerTime() {
    PlayerTime(
        currentPosition = 72_000L,
        duration = 215_000L
    )
}
