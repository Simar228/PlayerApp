package com.example.sound.Presentation.editSongInformation

import android.content.res.Configuration
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.editSongInformation.components.CustomDropdownField
import com.example.sound.Presentation.editSongInformation.components.CustomInputField
import com.example.sound.Presentation.editSongInformation.components.SongIconForEditSong
import com.example.sound.Presentation.editSongInformation.components.TopAppBarForEditSong
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongEvent
import com.example.sound.Presentation.editSongInformation.viewModel.EditSongViewModel


@Composable
fun EditSongScreen(
    song: Song,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    editSongViewModel: EditSongViewModel
){
    EditSongPreview(
        onEvent = editSongViewModel::sendEvent,
        onBackClick = onBackClick,
        onSaveClick = onSaveClick,
        title = editSongViewModel.title.collectAsStateWithLifecycle().value,
        artist = editSongViewModel.artist.collectAsStateWithLifecycle().value,
        album = editSongViewModel.album.collectAsStateWithLifecycle().value,
        genre = editSongViewModel.genre.collectAsStateWithLifecycle().value,
        art = song.art
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSongPreview(
    onEvent: (EditSongEvent) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    title: String,
    artist: String,
    album: String,
    genre: String,
    art: String?
) {

    Scaffold(
        topBar = {
            TopAppBarForEditSong(
                onBackClick = onBackClick,
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
                icon = art,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {}
            // 1. Все ваши верхние элементы формы (идут строго друг за другом)
            CustomInputField(
                label = "Название",
                value = title,
                onValueChange = { onEvent(EditSongEvent.EditSongTitle(it)) })
            CustomInputField(
                label = "Артист",
                value = artist,
                onValueChange = { onEvent(EditSongEvent.EditSongArtist(it)) })
            CustomInputField(
                label = "Альбом",
                value = album,
                onValueChange = { onEvent(EditSongEvent.EditSongAlbum(it)) })
            CustomDropdownField(label = "Жанр", value = genre, onClick = { /* ... */ })

            Spacer(modifier = Modifier.weight(1f))

            // 3. Кнопка "Сохранить"
            Button(
                onClick = { onSaveClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 20.dp,
                        horizontal = 12.dp
                    )
                    .height(54.dp),
                // Небольшой отступ сверху, когда кнопка прижата к полям
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
        onBackClick = {},
        onSaveClick = {},
        title = song.title.orEmpty(),
        artist = song.artist.orEmpty(),
        album = song.album.orEmpty(),
        art = song.art,
        genre = song.genre.orEmpty(),
        onEvent = {}
    )
}