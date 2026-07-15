package com.example.sound.Presentation.navigation

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.mainScreen.MainScreen
import com.example.sound.Presentation.mainScreen.MainViewModel
import com.example.sound.Presentation.playerUi.PlayerViewModel
import com.example.sound.Presentation.songPage.SongPage

@Composable
fun AppNavHost(
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    songs: List<Song>,
    modifier: Modifier = Modifier,
) {

    LaunchedEffect(songs.size) {
        Log.d("SongsDebug", "AppNavHost received ${songs.size} songs")
    }

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
                val mainViewModel: MainViewModel = viewModel()
                LaunchedEffect(songs) {
                    mainViewModel.setQueueSong(songs)
                }
                MainScreen(
                    mainViewModel = mainViewModel,
                    modifier = Modifier,
                    playerViewModel = playerViewModel
                )
            }
            composable<Routes.AlbumsRoute> {
                Log.d("Navigation", "albums is open")
            }

            composable<Routes.QueueRoute> {
                Log.d("Navigation", "queue is open")
            }
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
                onQueueClick = {},
                onEditClick = {},
                modifier = Modifier
            )
        }
    }
}
