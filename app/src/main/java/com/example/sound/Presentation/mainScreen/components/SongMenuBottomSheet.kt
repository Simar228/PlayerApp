package com.example.sound.Presentation.mainScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.sound.Domain.model.Song


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongMenuBottomSheet(
    song: Song,
    isFavorite: Boolean = false,
    onDismissRequest: () -> Unit,
    onPlayNextClick: () -> Unit,
    onAddToQueueClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onArtistClick: () -> Unit,
    onAlbumClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    fun executeAction(action: () -> Unit) {
        onDismissRequest()
        action()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        scrimColor = Color.Black.copy(alpha = 0.72f),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
        ) {
            SongMenuHeader(
                cover = if(song.art == null) null else rememberAsyncImagePainter(model = song.art),
                title = song.title ?: "empty title",
                artist = song.artist ?: "empty artist",
                album = song.album ?: "empty album",
                duration = song.duration,
            )

            HorizontalDivider(
                thickness = 1.dp,
            )

            SongMenuItem(
                icon = Icons.Outlined.PlayArrow,
                text = "Воспроизвести следующей",
                onClick = {
                    executeAction(onPlayNextClick)
                },
            )

            SongMenuItem(
                icon = Icons.AutoMirrored.Outlined.QueueMusic,
                text = "Добавить в очередь",
                onClick = {
                    executeAction(onAddToQueueClick)
                },
            )

            SongMenuItem(
                icon = if (isFavorite) {
                    Icons.Outlined.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                text = if (isFavorite) {
                    "Убрать из избранного"
                } else {
                    "Добавить в избранное"
                },
                onClick = {
                    executeAction(onFavoriteClick)
                },
            )

            SongMenuItem(
                icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
                text = "Добавить в плейлист",
                onClick = {
                    executeAction(onAddToPlaylistClick)
                },
            )

            HorizontalDivider(
                thickness = 1.dp,
            )

            SongMenuItem(
                icon = Icons.Outlined.PersonOutline,
                text = "Перейти к исполнителю",
                onClick = {
                    executeAction(onArtistClick)
                },
            )

            SongMenuItem(
                icon = Icons.Outlined.Album,
                text = "Перейти к альбому",
                onClick = {
                    executeAction(onAlbumClick)
                },
            )

            SongMenuItem(
                icon = Icons.Outlined.Edit,
                text = "Редактировать информацию",
                onClick = {
                    executeAction(onEditClick)
                },
            )
            SongMenuItem(
                icon = Icons.Outlined.Share,
                text = "Поделиться",
                onClick = {
                    executeAction(onShareClick)
                },
            )
        }
    }
}
