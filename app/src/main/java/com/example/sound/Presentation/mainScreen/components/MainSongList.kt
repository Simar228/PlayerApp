package com.example.sound.Presentation.mainScreen.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sound.Domain.model.Song

@Composable
fun MainSongList(
    songs: List<Song>,
    listState: LazyListState,
    onSongClick: (Song) -> Unit,
    onSongMenuClick: (Song) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            items = songs,
            key = { song -> song.id }
        ) { song ->
            MusicCard(
                song = song,
                onClick = { onSongClick(song) },
                onMenuClick = { onSongMenuClick(song) },
            )
        }

        item {
            Spacer(Modifier.size(250.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMainSongList() {
    MainSongList(
        songs = listOf(
            previewSong(
                id = "1",
                title = "Пока-пока",
                artist = "CUPSIZE",
                duration = 90_000L,
            ),
            previewSong(
                id = "2",
                title = "Следующая песня",
                artist = "Исполнитель",
                duration = 215_000L,
            ),
        ),
        listState = rememberLazyListState(),
        onSongClick = {},
        onSongMenuClick = {},
    )
}

private fun previewSong(
    id: String,
    title: String,
    artist: String,
    duration: Long,
): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        duration = duration,
        uri = "",
        album = null,
        genre = null,
        art = null,
    )
}
