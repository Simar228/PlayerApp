package com.example.sound.Presentation.editSongInformation

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Domain.model.Genre
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.editSongInformation.components.CustomInputField
import com.example.sound.Presentation.editSongInformation.components.EditableGenreDropdown
import com.example.sound.Presentation.editSongInformation.components.SongIconForEditSong
import com.example.sound.Presentation.editSongInformation.components.TopAppBarForEditSong
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongEvent
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongUiState
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongViewModel
import kotlinx.coroutines.launch


@Composable
fun EditSongScreen(
    popBackStack: () -> Unit,
    editSongViewModel: EditSongViewModel
) {
    val uiState = editSongViewModel.uiState.collectAsStateWithLifecycle().value
    EditSongPreview(
        uiState = uiState,
        onEvent = editSongViewModel::sendEvent,
        onBackClick = popBackStack,
        onSaveClick = editSongViewModel::saveSong,
        onResetClick = editSongViewModel::setSong,
        setArt = editSongViewModel::setArt,
        edited = editSongViewModel.edited.collectAsStateWithLifecycle().value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSongPreview(
    uiState: EditSongUiState,
    onEvent: (EditSongEvent) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onResetClick: () -> Unit,
    setArt: (String) -> Unit,
    edited: Boolean,
) {
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            setArt(uri.toString())
        }
    }
    val genresList = uiState.genres.map { genre ->
        genre.name
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBarForEditSong(
                onBackClick = onBackClick,
                onResetClick = onResetClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            SongIconForEditSong(
                icon = uiState.art,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onChangeClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )
            // 1. Все ваши верхние элементы формы (идут строго друг за другом)
            CustomInputField(
                label = "Название",
                value = uiState.title,
                onValueChange = { onEvent(EditSongEvent.EditSongTitle(it)) })
            CustomInputField(
                label = "Артист",
                value = uiState.artist,
                onValueChange = { onEvent(EditSongEvent.EditSongArtist(it)) })
            CustomInputField(
                label = "Альбом",
                value = uiState.album,
                onValueChange = { onEvent(EditSongEvent.EditSongAlbum(it)) })
            EditableGenreDropdown(
                label = "Жанр",
                currentValue = uiState.genre,
                suggestions = genresList,
                onValueChange = { onEvent(EditSongEvent.EditSongGenre(it)) },
            )

            Spacer(modifier = Modifier.weight(1f))


            Button(
                enabled = edited,
                onClick = {
                    scope.launch {
                        onSaveClick()
                        onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 20.dp,
                        horizontal = 12.dp
                    )
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Сохранить", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun EditSongScreenPreview() {
    val song = Song(
        id = "song-1",
        title = "First Song",
        artist = "First Artist",
        duration = 180_000L,
        uri = "EMPTY",
        album = "First Album",
        genre = "Pop",
    )
    EditSongPreview(
        uiState = EditSongUiState(
            title = song.title.orEmpty(),
            artist = song.artist.orEmpty(),
            album = song.album.orEmpty(),
            art = song.art,
            genre = song.genre.orEmpty(),
        ),
        onBackClick = {},
        onSaveClick = {},
        onEvent = {},
        setArt = {},
        onResetClick = {},
        edited = false
    )
}
