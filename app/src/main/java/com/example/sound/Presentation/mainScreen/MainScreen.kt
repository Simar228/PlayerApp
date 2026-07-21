package com.example.sound.Presentation.mainScreen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sound.Presentation.mainScreen.components.MusicCard
import com.example.sound.Presentation.mainScreen.components.SortButton
import com.example.sound.Presentation.playerUi.PlayerViewModel
import com.example.sound.R


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
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    modifier = Modifier.size(34.dp),
                    painter = painterResource(R.drawable.baseline_menu),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(
                    modifier = Modifier.weight(0.2f)
                )
                Text(
                    stringResource(R.string.all_songs),
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(
                    modifier = Modifier.weight(0.6f)
                )
                Icon(
                    modifier = Modifier.size(34.dp),
                    painter = painterResource(R.drawable.outline_search_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(
                    modifier = Modifier.weight(0.2f)
                )
                Icon(
                    modifier = Modifier.size(34.dp),
                    painter = painterResource(R.drawable.outline_more_vert_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 15.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .padding(horizontal = 15.dp),
            ) {
                item {
                    SortButton(
                        isActive = currentButton[0].isActive,
                        text = stringResource(R.string.sort_by_name),
                        isUp = currentButton[0].isUp
                    ) {
                        mainViewModel.sortQueueSong(
                            MainSortScreenEvents.SortByTitle(currentButton[0].isUp)
                        )
                    }
                }
                item {
                    SortButton(
                        isActive = currentButton[1].isActive,
                        text = stringResource(R.string.sort_by_artist),
                        isUp = currentButton[1].isUp
                    ) {
                        mainViewModel.sortQueueSong(
                            MainSortScreenEvents.SortByArtist(currentButton[1].isUp)
                        )
                    }
                }
                item {
                    SortButton(
                        isActive = currentButton[2].isActive,
                        text = stringResource(R.string.sort_by_album),
                        isUp = currentButton[2].isUp
                    ) {
                        mainViewModel.sortQueueSong(
                            MainSortScreenEvents.SortByAlbum(currentButton[2].isUp)
                        )

                    }
                }
                item {
                    SortButton(
                        isActive = currentButton[3].isActive,
                        text = stringResource(R.string.sort_by_genre),
                        isUp = currentButton[3].isUp
                    ) {
                        mainViewModel.sortQueueSong(
                            MainSortScreenEvents.SortByGenre(currentButton[3].isUp)
                        )
                    }
                }
            }
            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(7f)
            ) {
                items(
                    items = songs,
                    key = { song -> song.id }
                ) { song ->
                    MusicCard(
                        song = song,
                        onClick = {
                            playerViewModel.sendSong(songs, song)
                        },
                        onMenuClick = {
                            mainViewModel.openSongMenuBottomSheet(song.id)
                        },
                    )
                }
                item {
                    Spacer(
                        Modifier.size(250.dp)
                    )
                }
            }
        }
    }
}


@Composable
@Preview
private fun PreviewMainScreen() {
    val context = LocalContext.current
    MainScreen(
        playerViewModel = viewModel(),
        modifier = Modifier,
        mainViewModel = viewModel()
    )
}
