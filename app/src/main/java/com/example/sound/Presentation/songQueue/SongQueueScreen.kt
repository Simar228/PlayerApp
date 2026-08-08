package com.example.sound.Presentation.songQueue

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Domain.model.Song
import com.example.sound.ui.theme.SoundTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun SongQueueScreen(
    songQueueViewModel: SongQueueViewModel,
    currentSong: Song?,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    onSongClick: (Song, Long) -> Unit,
    modifier: Modifier,
    onDeleteSong: (Long) -> Unit,
) {
    SongQueueView(
        currentSong = currentSong,
        isPlaying = isPlaying,
        modifier = modifier,
        onBackClick = onBackClick,
        onClearClick = onClearClick,
        onSongClick = onSongClick,
        onDeleteSong = onDeleteSong,
        saveQueueOrder =  songQueueViewModel::saveQueueOrder,
        currentSongQueue = songQueueViewModel.songQueue.collectAsStateWithLifecycle().value,
        moveQueueItem = songQueueViewModel::moveQueueItem
    )
}

@Composable
fun SongQueueView(
    currentSong: Song?,
    isPlaying: Boolean,
    modifier: Modifier,
    currentSongQueue: List<QueueItemUi>,
    onBackClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onSongClick: (Song, Long) -> Unit,
    onDeleteSong: (Long) -> Unit,
    moveQueueItem: (Int, Int) -> Unit,
    saveQueueOrder: () -> Unit,
) {

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState
    ) { from, to ->
        moveQueueItem(from.index, to.index)
    }
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
                    isMain = true,
                    song = currentSong,
                    onClick = {},
                    onDelete = {},
                    isPlaying = isPlaying
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
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = currentSongQueue,
                    key = { it.queueItemId }
                ) { queueItem ->
                    ReorderableItem(
                        state = reorderableState,
                        key = queueItem.queueItemId
                    ) { _ ->
                        Column {
                            MusicQueueCard(
                                song = queueItem.song,
                                onClick = {
                                    onSongClick(queueItem.song, queueItem.queueItemId)
                                },
                                onDelete = {
                                    onDeleteSong(queueItem.queueItemId)
                                },
                                isMain = false,
                                dragHandleModifier =
                                    Modifier.draggableHandle(
                                        onDragStopped = {
                                            saveQueueOrder()
                                        }
                                    ),
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
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

@Preview(
    name = "Song queue",
    showBackground = true,
    widthDp = 412,
    heightDp = 892,
)
@Composable
private fun SongQueueViewPreview() {
    val currentSong = Song(
        id = "current-song",
        title = "Now Playing",
        artist = "Current Artist",
        duration = 215_000L,
        uri = Uri.EMPTY,
        album = "Current Album",
        genre = "Rock",
    )

    val queueItems = listOf(
        QueueItemUi(
            queueItemId = 1L,
            song = Song(
                id = "song-1",
                title = "First Song",
                artist = "First Artist",
                duration = 180_000L,
                uri = Uri.EMPTY,
                album = "First Album",
                genre = "Pop",
            ),
        ),
        QueueItemUi(
            queueItemId = 2L,
            song = Song(
                id = "song-2",
                title = "Second Song",
                artist = "Second Artist",
                duration = 240_000L,
                uri = Uri.EMPTY,
                album = "Second Album",
                genre = "Rock",
            ),
        ),
        QueueItemUi(
            queueItemId = 3L,
            song = Song(
                id = "song-3",
                title = "Third Song",
                artist = "Third Artist",
                duration = 195_000L,
                uri = Uri.EMPTY,
                album = "Third Album",
                genre = "Electronic",
            ),
        ),
    )

    SoundTheme(darkTheme = true) {
        SongQueueView(
            currentSong = currentSong,
            isPlaying = true,
            modifier = Modifier.fillMaxSize(),
            currentSongQueue = queueItems,
            onBackClick = {},
            onClearClick = {},
            onSongClick = { _, _ -> },
            onDeleteSong = {},
            moveQueueItem = { _, _ -> },
            saveQueueOrder = {},
        )
    }
}


