package com.example.sound.Presentation.songPage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.ui.theme.SoundTheme

@Composable
fun PlayerBottomActions(
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
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
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

@Preview
@Composable
private fun PreviewPlayerBottomActions() {
    SoundTheme(darkTheme = true) {
        PlayerBottomActions(
            shuffleEnabled = true,
            repeatEnabled = true,
            onQueueClick = {},
            onRepeatClick = {},
            onShuffleClick = {},
            onEditClick = {}
        )
    }
}

@Preview
@Composable
private fun PreviewPlayerAction() {
    SoundTheme(darkTheme = true) {
        PlayerAction(
            icon = {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null
                )
            },
            title = "Повтор",
            stateText = "Вкл.",
            active = true,
            onClick = {}
        )
    }
}
