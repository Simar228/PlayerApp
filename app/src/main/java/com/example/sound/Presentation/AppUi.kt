package com.example.sound.Presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.navigation.AppNavHost
import com.example.sound.Presentation.navigation.Routes
import com.example.sound.Presentation.navigation.bottom.BottomNavigation
import com.example.sound.Presentation.playerUi.PlayerUI
import com.example.sound.Presentation.playerUi.PlayerViewModel

@Composable
fun AppUi(
    songs: List<Song>,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(songs.size) {
        Log.d("SongsDebug", "AppUi received ${songs.size} songs")
    }
    val playerViewModel: PlayerViewModel = viewModel()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val visibleEntries by navController.visibleEntries
        .collectAsStateWithLifecycle()
    val isMainScreen = currentDestination
        ?.hierarchy
        ?.any { destination ->
            destination.hasRoute<Routes.MainGraph>()
        }
        ?: false


    val isSongPageStillVisible = visibleEntries.any { entry ->
        entry.destination.hasRoute<Routes.SongPageRoute>()
    }

    val showBottomBar =
        isMainScreen && !isSongPageStillVisible


    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    songs.getOrNull(0)?.let {
                        PlayerUI(
                            viewModel = playerViewModel,
                            onClick = {
                                navController.navigate(Routes.SongPageRoute) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    BottomNavigation(
                        navController = navController,
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            playerViewModel = playerViewModel,
            navController = navController,
            songs = songs,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
