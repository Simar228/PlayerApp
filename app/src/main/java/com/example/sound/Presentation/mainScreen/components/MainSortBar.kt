package com.example.sound.Presentation.mainScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sound.Presentation.mainScreen.MainSortScreenEvents
import com.example.sound.R

@Composable
fun MainSortBar(
    buttons: List<SortButtonValue>,
    onSort: (MainSortScreenEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = listOf(
        stringResource(R.string.sort_by_name),
        stringResource(R.string.sort_by_artist),
        stringResource(R.string.sort_by_album),
        stringResource(R.string.sort_by_genre),
    )

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(
            space = 15.dp,
            alignment = Alignment.CenterHorizontally
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
    ) {
        items(
            items = buttons,
            key = { button -> button.index }
        ) { button ->
            SortButton(
                isActive = button.isActive,
                text = labels[button.index],
                isUp = button.isUp,
                onClick = {
                    onSort(button.toSortEvent())
                }
            )
        }
    }
}

private fun SortButtonValue.toSortEvent(): MainSortScreenEvents {
    return when (index) {
        0 -> MainSortScreenEvents.SortByTitle(isUp)
        1 -> MainSortScreenEvents.SortByArtist(isUp)
        2 -> MainSortScreenEvents.SortByAlbum(isUp)
        3 -> MainSortScreenEvents.SortByGenre(isUp)
        else -> error("Unsupported sort button index: $index")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMainSortBar() {
    MainSortBar(
        buttons = listOf(
            SortButtonValue(index = 0, isUp = true, isActive = true),
            SortButtonValue(index = 1, isUp = true, isActive = false),
            SortButtonValue(index = 2, isUp = true, isActive = false),
            SortButtonValue(index = 3, isUp = true, isActive = false),
        ),
        onSort = {}
    )
}
