package com.example.sound.Presentation.playerUi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

@Composable
fun CompactPlayerArtwork(
    artwork: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (artwork != null) {
            SubcomposeAsyncImage(
                model = artwork,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { CompactPlayerArtworkPlaceholder() },
                error = { CompactPlayerArtworkPlaceholder() }
            )
        } else {
            CompactPlayerArtworkPlaceholder()
        }
    }
}

@Composable
private fun CompactPlayerArtworkPlaceholder() {
    Icon(
        imageVector = Icons.Rounded.MusicNote,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewCompactPlayerArtwork() {
    CompactPlayerArtwork(
        artwork = null,
        modifier = Modifier.size(70.dp)
    )
}
