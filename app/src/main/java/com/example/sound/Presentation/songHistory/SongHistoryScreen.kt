package com.example.sound.Presentation.songHistory

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.Domain.model.HistoryItem
import com.example.sound.Presentation.mainScreen.components.MusicCard
import com.example.sound.R
import com.example.sound.ui.theme.SoundTheme
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SongHistoryScreen(
    viewModel: HistorySongViewModel,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        SongHistoryTopBar(
            onBackClick = onBackClick,
            onClearClick = onClearClick,
            historySongMap = viewModel.historyQueue.collectAsStateWithLifecycle().value
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SongHistoryTopBar(
    historySongMap: Map<String, List<HistoryItem>>,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column() {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

            Text(
                text = stringResource(R.string.song_history),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
            )

            IconButton(onClick = onClearClick) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.clear_song_history),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
        LazyColumn() {
            historySongMap.forEach { (date, historyItemList) ->
                item {
                    DateDivider(date)
                }
                items(
                    items = historyItemList,
                    key = { it.song.id + it.position }
                ) { historySong ->
                    MusicCard(
                        song = historySong.song,
                        onClick = {},
                        onMenuClick = {},
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
    name = "Empty song history",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
)
@Composable
private fun SongHistoryScreenPreview() {
    SoundTheme(darkTheme = true) {
        SongHistoryTopBar(
            onBackClick = {},
            onClearClick = {},
            historySongMap = emptyMap(),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateDivider(
    date: String,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

    val title = remember(date, locale) {
        runCatching {
            val parsedDate = MonthDay.parse(
                date,
                DateTimeFormatter.ofPattern("dd.MM")
            )

            val month = parsedDate.month.getDisplayName(
                TextStyle.FULL,
                locale
            )

            val formattedMonth = month.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(locale) else it.toString()
            }

            "${parsedDate.dayOfMonth} $formattedMonth"
        }.getOrElse {
            date
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f))

        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        HorizontalDivider(Modifier.weight(1f))
    }
}
