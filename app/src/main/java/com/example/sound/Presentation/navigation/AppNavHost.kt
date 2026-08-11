package com.example.sound.Presentation.navigation

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.mainScreen.MainNavigationEvents
import com.example.sound.Presentation.mainScreen.MainScreen
import com.example.sound.Presentation.mainScreen.MainViewModel
import com.example.sound.Presentation.mainScreen.components.SongMenuBottomSheet
import com.example.sound.Presentation.playerUi.viewModel.PlayerViewModel
import com.example.sound.Presentation.songPage.SongPage
import com.example.sound.Presentation.songQueue.SongQueueScreen
import com.example.sound.Presentation.songQueue.SongQueueViewModel

@Composable
fun AppNavHost(
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    songs: List<Song>,
    modifier: Modifier = Modifier,
) {
    val songQueueViewModel: SongQueueViewModel = viewModel()
    val mainViewModel: MainViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.MainGraph,
        enterTransition = {
            fadeIn(animationSpec = tween(250))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(250))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(250))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(250))
        }
    ) {
        navigation<Routes.MainGraph>(
            startDestination = Routes.SongsRoute
        ) {

            composable<Routes.SongsRoute> {
                LaunchedEffect(mainViewModel) {
                    mainViewModel.mainNavigationEvents.collect { events ->
                        when (events) {
                            is MainNavigationEvents.OpenSongMenuBottomSheet -> {
                                navController.navigate(Routes.SongBottomSheet(events.songId))
                            }
                        }
                    }
                }
                LaunchedEffect(songs) {
                    mainViewModel.setQueueSong(songs)
                }
                MainScreen(
                    mainViewModel = mainViewModel,
                    modifier = modifier,
                    playerViewModel = playerViewModel
                )
            }

            composable<Routes.AlbumsRoute> {
                Log.d("Navigation", "albums is open")
            }

            composable<Routes.QueueRoute> {
                val playerUiState by playerViewModel.uiState.collectAsStateWithLifecycle()
                SongQueueScreen(
                    isPlaying = playerUiState.isPlaying,
                    currentSong = playerUiState.currentSong,
                    songQueueViewModel = songQueueViewModel,
                    onBackClick = { navController.popBackStack() },
                    onClearClick = { songQueueViewModel.clearSongQueue() },
                    onSongClick = { song, queueItemId ->
                        playerViewModel.sendSong(
                            song = song,
                            queueItemId = queueItemId
                        )
                    },
                    modifier = modifier,
                    onDeleteSong = { queueItemId ->
                        songQueueViewModel.deleteQueueItem(queueItemId)
                    }
                )
            }
        }

        dialog<Routes.SongBottomSheet> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.SongBottomSheet>()
            val song = songs.firstOrNull { it.id == route.songId }
            if (song == null) {
                LaunchedEffect(route.songId) {
                    navController.popBackStack()
                }
                return@dialog
            }
            SongMenuBottomSheet(
                isFavorite = false,
                onDismissRequest = {
                    navController.popBackStack()
                },

                onPlayNextClick = {
                    songQueueViewModel.chooseNextSong(song)
                },

                onAddToQueueClick = {
                    songQueueViewModel.addSongToQueue(song)
                },

                onFavoriteClick = {
                    // mainViewModel.toggleFavorite(song.id)
                },

                onAddToPlaylistClick = {
                    // открыть выбор плейлиста
                },

                onArtistClick = {
                    // navController.navigate(Routes.Artist(song.artist))
                },

                onAlbumClick = {
                    // navController.navigate(Routes.Album(song.album))
                },

                onEditClick = {
                    // открыть редактирование
                },

                onShareClick = {
                    // отправить Intent.ACTION_SEND
                },
                song = song
            )
        }

        composable<Routes.SongPageRoute> {
            SongPage(
                viewModel = playerViewModel,
                isFavorite = true,
                shuffleEnabled = true,
                repeatEnabled = true,
                onCloseClick = {
                    navController.popBackStack()
                },
                onMenuClick = {},
                onFavoriteClick = {},
                onShuffleClick = {},
                onRepeatClick = {},
                onQueueClick = {
                    navController.navigate(Routes.QueueRoute) {
                        launchSingleTop = true
                    }
                },
                onEditClick = {},
                modifier = Modifier
            )
        }
    }
}


const val TAG = "AppNavHost"
