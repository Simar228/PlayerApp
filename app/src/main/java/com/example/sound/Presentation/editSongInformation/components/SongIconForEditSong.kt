package com.example.sound.Presentation.editSongInformation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage


@Composable
fun SongIconForEditSong(
    modifier: Modifier,
    icon: String?,
    onChangeClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(400.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            SubcomposeAsyncImage(
                model = icon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { MusicArtworkPlaceholder() },
                error = { MusicArtworkPlaceholder() }
            )
        } else {
            MusicArtworkPlaceholder()
        }

        // Кнопка камеры поверх обложки
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(80.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .clickable { onChangeClick() },
        ){
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = android.R.drawable.ic_menu_camera),
                contentDescription = "Камера",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun MusicArtworkPlaceholder() {
    Icon(
        imageVector = Icons.Rounded.MusicNote,
        contentDescription = null,
        modifier = Modifier.size(160.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview
@Composable
private fun PreviewSongIconForEditSong() {
    SongIconForEditSong(
        Modifier,
        icon = null
    ) { }
}