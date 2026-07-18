package com.example.sound

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.AppUi
import com.example.sound.Presentation.SongsUiState
import com.example.sound.Presentation.errorScreen.ErrorScreen
import com.example.sound.Presentation.loadingScreen.LoadingScreen
import com.example.sound.Presentation.permissionScreen.PermissionScreen
import com.example.sound.ui.theme.SoundTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@AndroidEntryPoint
class MainActivity() : ComponentActivity() {
    @Inject
    lateinit var songRepository: SongRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SoundTheme {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()

                var songsUiState by remember {
                    mutableStateOf<SongsUiState>(SongsUiState.Loading)
                }

                val permission =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }

                fun loadSongs() {
                    coroutineScope.launch {
                        Log.d(SONGS_DEBUG_TAG, "loadSongs() started")
                        songsUiState = SongsUiState.Loading
                        try {
                            val loadedSongs = withContext(Dispatchers.IO) {
                                songRepository.getSong()
                            }
                            Log.d(
                                SONGS_DEBUG_TAG,
                                "loadSongs() returned ${loadedSongs.size} songs"
                            )
                            songsUiState = SongsUiState.Success(loadedSongs)
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            songsUiState = SongsUiState.Error(exception.toString())
                        }
                    }
                }

                val audioPermissionLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        val checkResult = ContextCompat.checkSelfPermission(
                            context,
                            permission
                        )
                        Log.d(
                            SONGS_DEBUG_TAG,
                            "Permission result: granted=$granted, " +
                                    "checkSelfPermission=$checkResult"
                        )
                        if (granted) {
                            loadSongs()
                        } else {
                            songsUiState = SongsUiState.PermissionDenied
                        }
                    }

                val appSettingsLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult()
                    ) {
                        val permissionGranted =
                            ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED

                        if (permissionGranted) {
                            loadSongs()
                        } else {
                            songsUiState = SongsUiState.PermissionDenied
                        }
                    }

                LaunchedEffect(Unit) {
                    val checkResult = ContextCompat.checkSelfPermission(
                        context,
                        permission
                    )
                    val permissionGranted =
                        checkResult == PackageManager.PERMISSION_GRANTED

                    Log.d(
                        SONGS_DEBUG_TAG,
                        "Android SDK=${Build.VERSION.SDK_INT}, " +
                                "permission=$permission, " +
                                "checkSelfPermission=$checkResult, " +
                                "granted=$permissionGranted"
                    )

                    if (permissionGranted) {
                        loadSongs()
                    } else {
                        audioPermissionLauncher.launch(permission)
                    }
                }
                when (val state = songsUiState) {
                    SongsUiState.Loading -> {
                        LoadingScreen()
                    }

                    SongsUiState.PermissionDenied -> {
                        PermissionScreen(
                            onOpenSettings = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    "package:${context.packageName}".toUri()
                                )

                                appSettingsLauncher.launch(intent)
                            }
                        )
                    }

                    is SongsUiState.Success -> {
                        AppUi(songs = state.songs)
                    }

                    is SongsUiState.Error -> {
                        ErrorScreen(
                            onRetry = {
                                loadSongs()
                            }
                        )
                    }
                }
            }
        }
    }
}

private const val SONGS_DEBUG_TAG = "SongsDebug"
