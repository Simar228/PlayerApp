package com.example.sound.Presentation.mainScreen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.mainScreen.components.MainSongList
import com.example.sound.Presentation.mainScreen.components.MainSortBar
import com.example.sound.Presentation.mainScreen.components.MainTopBar
import com.example.sound.Presentation.mainScreen.components.SortButtonValue
import com.example.sound.Presentation.playerUi.viewModel.PlayerViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier,
    onSongMenuClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onSongHistoryClick: () -> Unit,
) {
    MainScreenRoute(
        songs = mainViewModel.songsQueue.collectAsStateWithLifecycle().value,
        currentButtons = mainViewModel.currentDirectionOfSort.collectAsStateWithLifecycle().value,
        modifier = modifier,
        onSongMenuClick = onSongMenuClick,
        onSettingsClick = onSettingsClick,
        onSongHistoryClick = onSongHistoryClick,
        sendSong = playerViewModel::playFromLibrary,
        sortQueueSong = mainViewModel::sortQueueSong,
    )
}

@Composable
private fun MainScreenRoute(
    songs: List<Song>,
    currentButtons: List<SortButtonValue>,
    modifier: Modifier,
    onSongMenuClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onSongHistoryClick: () -> Unit,
    sortQueueSong: (MainSortScreenEvents) -> Unit,
    sendSong: (Song, List<Song>) -> Unit
) {

    val lazyColumnState = rememberLazyListState()
    LaunchedEffect(songs.size) {
        Log.d("SongsDebug", "MainScreen received ${songs.size} songs")
    }
    LaunchedEffect(songs) {
        lazyColumnState.scrollToItem(0)
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column() {
            MainTopBar(
                modifier = Modifier.weight(0.7f),
                onSettingsClick = onSettingsClick,
                onSongHistoryClick = onSongHistoryClick,
            )
            MainSortBar(
                buttons = currentButtons,
                onSort = sortQueueSong,
                modifier = Modifier
                    .weight(0.7f)
            )
            MainSongList(
                songs = songs,
                listState = lazyColumnState,
                onSongClick = { song ->
                    sendSong(song, songs)
                },
                onSongMenuClick = onSongMenuClick,
                modifier = Modifier
                    .weight(7f)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewMainScreen() {
    val songs = listOf(
        Song(
            id = "song-1",
            title = "First Song",
            artist = "First Artist",
            duration = 180_000L,
            uri = "EMPTY",
            album = "First Album",
            genre = "Pop",
        ),
        Song(
            id = "song-2",
            title = "Second Song",
            artist = "Second Artist",
            duration = 240_000L,
            uri = "EMPTY",
            album = "Second Album",
            genre = "Rock",
        ),
        Song(
            id = "song-3",
            title = "Third Song",
            artist = "Third Artist",
            duration = 195_000L,
            uri = "EMPTY",
            album = "Third Album",
            genre = "Electronic",

            )
    )
    MainScreenRoute(
        songs = songs,
        currentButtons = mutableListOf(
            SortButtonValue(
                0,
                true,
                true,
            ),
            SortButtonValue(
                1,
                true,
                false
            ),
            SortButtonValue(
                2,
                true,
                false,
            ),
            SortButtonValue(
                3,
                true,
                false
            ),
        ),
        modifier = Modifier,
        onSongMenuClick = {},
        onSettingsClick = {},
        onSongHistoryClick = {},
        sortQueueSong = {},
        sendSong = { song, songs ->

        }
    )
}
