package com.example.sound.Presentation.songQueue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.sound.Domain.model.Song
import com.example.sound.Presentation.songQueue.components.SwipeRevealDelete
import com.example.sound.Presentation.songQueue.utills.AnimatedPlayingBars

@Composable
fun MusicQueueCard(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isMain: Boolean,
    song: Song,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    dragHandleModifier: Modifier = Modifier,
) {

    SwipeRevealDelete(
        isActive = !isMain,
        onDelete = { onDelete() },
        modifier = modifier,
    ) {
        Row(
            modifier = modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .height(120.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(75.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (song.art != null) {
                    SubcomposeAsyncImage(
                        model = song.art,
                        contentDescription = "Обложка ${song.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = { MusicArtworkPlaceholder() },
                        error = { MusicArtworkPlaceholder() }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))


            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title ?: "Нету названия",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = song.artist ?: "Нету исполнителя",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))


            Text(
                text = formatDuration(song.duration),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = {},
                modifier = dragHandleModifier.size(70.dp)
            ) {
                if (isMain) {
                    AnimatedPlayingBars(
                        isPlaying = isPlaying,
                        modifier = Modifier.size(25.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Меню песни",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicArtworkPlaceholder() {
    Icon(
        imageVector = Icons.Rounded.MusicNote,
        contentDescription = null,
        modifier = Modifier.size(26.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%d:%02d".format(minutes, seconds)
}

@Preview
@Composable
private fun PreviewMusicCard() {
    MusicQueueCard(
        song = Song(
            id = "",
            title = "Пока-Пока",
            artist = "CUPSIZE",
            duration = 90000,
            uri = "EMPTY",
            album = "кажется, в аду прикольно, но меня выгнали б утром",
            genre = "Инди-Рок",
            art = null
        ),
        onClick = { },
        onDelete = {},
        isMain = true,
    )
}

