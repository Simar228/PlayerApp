package com.example.sound

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import com.example.sound.Domain.model.Song
import com.example.sound.Domain.repository.SongRepository
import com.example.sound.Presentation.AppUi
import com.example.sound.ui.theme.SoundTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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

                var songs by remember {
                    mutableStateOf<List<Song>>(emptyList())
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
                        val loadedSongs = withContext(Dispatchers.IO) {
                            songRepository.getSong()
                        }
                        Log.d(
                            SONGS_DEBUG_TAG,
                            "loadSongs() returned ${loadedSongs.size} songs"
                        )
                        songs = loadedSongs
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
                            // Пользователь отказал в доступе
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

                AppUi(
                    songs = songs
                )
            }
        }
    }
}

private const val SONGS_DEBUG_TAG = "SongsDebug"
