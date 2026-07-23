package com.example.sound.Presentation.songQueue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.playerUi.PlayerViewModel


@Composable
fun SongQueueScreen(
    currentSong: Song?,
    modifier: Modifier,
    songQueueViewModel: SongQueueViewModel,
    onBackClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onSongClick: (Song, Int) -> Unit,
    onShuffleClick: () -> Unit = {},
    onSaveQueueClick: () -> Unit = {}
) {

    val currentSongQueue by songQueueViewModel.sonqQueue.collectAsStateWithLifecycle()
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QueueHeader(
                onBackClick = onBackClick,
                onClearClick = onClearClick
            )

            Text(
                text = "Сейчас играет",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 14.dp, top = 6.dp, bottom = 4.dp)
            )
            currentSong?.let {
                MusicQueueCard(
                    song = currentSong,
                    onClick = {},
                    onMenuClick = {},
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 14.dp),
            )

            Text(
                text = "Далее в очереди (${currentSongQueue.size})",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 14.dp, top = 14.dp, bottom = 4.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                itemsIndexed(
                    items = currentSongQueue,
                    key = {_, it -> it.queueItemId }
                ) { index, queueItem ->
                    MusicQueueCard(
                        song = queueItem.song,
                        onClick = { onSongClick(queueItem.song, index) },
                        onMenuClick = {},
                    )
                    HorizontalDivider()
                }
            }

            HorizontalDivider()
            QueueBottomActions(
                onShuffleClick = onShuffleClick,
                onSaveQueueClick = onSaveQueueClick
            )
        }
    }
}

@Composable
private fun QueueHeader(
    onBackClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                modifier = Modifier.size(50.dp),
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Закрыть очередь",
                tint = Color.White
            )
        }

        Text(
            text = "Очередь",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Очистить",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClearClick)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ArtworkPlaceholder() {
    Icon(
        imageVector = Icons.Default.MusicNote,
        contentDescription = null,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun QueueBottomActions(
    onShuffleClick: () -> Unit,
    onSaveQueueClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QueueAction(
            text = "Перемешать",
            icon = { Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(19.dp)) },
            onClick = onShuffleClick,
            modifier = Modifier.weight(1f)
        )
        QueueAction(
            text = "Сохранить очередь",
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.PlaylistAdd,
                    null,
                    modifier = Modifier.size(19.dp)
                )
            },
            onClick = onSaveQueueClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QueueAction(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}




