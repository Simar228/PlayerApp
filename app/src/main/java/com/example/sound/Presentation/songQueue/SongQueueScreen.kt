package com.example.sound.Presentation.songQueue

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Domain.model.QueueItem
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.songQueue.components.QueueHeader
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
    currentSongQueue: List<QueueItem>,
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
                    key = { it.id }
                ) { queueItem ->
                    ReorderableItem(
                        state = reorderableState,
                        key = queueItem.id
                    ) { _ ->
                        Column {
                            MusicQueueCard(
                                song = queueItem.song,
                                onClick = {
                                    onSongClick(queueItem.song, queueItem.id)
                                },
                                onDelete = {
                                    onDeleteSong(queueItem.id)
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
        uri = "EMPTY",
        album = "Current Album",
        genre = "Rock",
    )

    val queueItems = listOf(
        QueueItem(
            song = Song(
                id = "song-1",
                title = "First Song",
                artist = "First Artist",
                duration = 180_000L,
                uri = "EMPTY",
                album = "First Album",
                genre = "Pop",
            ),
            id = 1,
            position = 0,
            fromUser = true,
        ),
        QueueItem(
            id = 2,
            position = 1,
            song = Song(
                id = "song-2",
                title = "Second Song",
                artist = "Second Artist",
                duration = 240_000L,
                uri = "EMPTY",
                album = "Second Album",
                genre = "Rock",
            ),
            fromUser = true
        ),
        QueueItem(
            id = 3,
            position = 2,
            song = Song(
                id = "song-3",
                title = "Third Song",
                artist = "Third Artist",
                duration = 195_000L,
                uri = "EMPTY",
                album = "Third Album",
                genre = "Electronic",
            ),
            fromUser = true
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


