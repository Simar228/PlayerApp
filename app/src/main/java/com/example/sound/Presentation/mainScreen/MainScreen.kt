package com.example.sound.Presentation.mainScreen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Presentation.mainScreen.components.MainSortBar
import com.example.sound.Presentation.mainScreen.components.MainSongList
import com.example.sound.Presentation.mainScreen.components.MainTopBar
import com.example.sound.Presentation.playerUi.viewModel.PlayerViewModel


@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier,
) {

    val lazyColumnState = rememberLazyListState()
    val songs by mainViewModel.songsQueue.collectAsStateWithLifecycle()
    LaunchedEffect(songs.size) {
        Log.d("SongsDebug", "MainScreen received ${songs.size} songs")
    }
    LaunchedEffect(songs) {
        lazyColumnState.scrollToItem(0)
    }

    val currentButton by mainViewModel.currentDirectionOfSort.collectAsStateWithLifecycle()
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column() {
            MainTopBar(
                modifier = Modifier
                    .weight(0.7f)
            )
            MainSortBar(
                buttons = currentButton,
                onSort = mainViewModel::sortQueueSong,
                modifier = Modifier
                    .weight(0.7f)
            )
            MainSongList(
                songs = songs,
                listState = lazyColumnState,
                onSongClick = { song ->
                    playerViewModel.sendSong(songs, song)
                },
                onSongMenuClick = { song ->
                    mainViewModel.openSongMenuBottomSheet(song.id)
                },
                modifier = Modifier
                    .weight(7f)
            )
        }
    }
}
