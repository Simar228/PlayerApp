package com.example.sound.Presentation.mainScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
private fun SongMenuHeader(
    cover: Painter?,
    title: String,
    artist: String,
    album: String,
    duration: Long,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 18.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (cover != null) {
            Image(
                painter = cover,
                contentDescription = "Обложка песни",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(10.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF26313A)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "$artist · $album",
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = duration.toTimeString(),
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun SongMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            text = text,
            fontSize = 18.sp,
        )
    }
}

fun Long.toTimeString(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
