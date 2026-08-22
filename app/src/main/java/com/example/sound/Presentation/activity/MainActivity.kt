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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Presentation.AppUi
import com.example.sound.Presentation.SongsUiState
import com.example.sound.Presentation.activity.MainActivityViewModel
import com.example.sound.Presentation.errorScreen.ErrorScreen
import com.example.sound.Presentation.loadingScreen.LoadingScreen
import com.example.sound.Presentation.permissionScreen.PermissionScreen
import com.example.sound.ui.theme.SoundTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity() : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainActivityViewModel = hiltViewModel()

            SoundTheme {
                val context = LocalContext.current
                val songsUiState by viewModel.songsUiState.collectAsStateWithLifecycle()
                val permission =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }

                //Запрос разрешения на доступ к песням (переменная)
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
                            viewModel.loadSongs()//:TODO
                        } else {
                            viewModel.permissionDenied()
                        }
                    }


                //Переход в настройки дабы включить разрешения(переменная)
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
                            viewModel.setSongsUiState()
                            viewModel.loadSongs()//:TODO
                        }
                    }

                //Запуск запроса разрешения к песням
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
                        viewModel.loadSongs() //:TODO
                    } else {
                        audioPermissionLauncher.launch(permission)
                    }
                }


                //Обработка songsUiState
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
                        AppUi()
                    }

                    is SongsUiState.Error -> {
                        ErrorScreen(
                            onRetry = {
                                viewModel.loadSongs() //:TODO
                            }
                        )
                    }
                }
            }
        }
    }
}

private const val SONGS_DEBUG_TAG = "SongsDebug"
