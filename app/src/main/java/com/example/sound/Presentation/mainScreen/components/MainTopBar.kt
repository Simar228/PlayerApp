package com.example.sound.Presentation.mainScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sound.R

@Composable
fun MainTopBar(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {},
    onSongHistoryClick: () -> Unit = {},
) {
    var isMoreMenuExpanded by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Icon(
            modifier = Modifier.size(34.dp),
            painter = painterResource(R.drawable.baseline_menu),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(
            modifier = Modifier.weight(0.2f)
        )
        Text(
            stringResource(R.string.all_songs),
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(
            modifier = Modifier.weight(0.6f)
        )
        Icon(
            modifier = Modifier.size(34.dp),
            painter = painterResource(R.drawable.outline_search_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )
        Spacer(
            modifier = Modifier.weight(0.2f)
        )
        Box {
            IconButton(
                onClick = { isMoreMenuExpanded = true },
            ) {
                Icon(
                    modifier = Modifier.size(34.dp),
                    painter = painterResource(R.drawable.outline_more_vert_24),
                    contentDescription = stringResource(R.string.more_actions),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            DropdownMenu(
                expanded = isMoreMenuExpanded,
                onDismissRequest = { isMoreMenuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.settings)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        isMoreMenuExpanded = false
                        onSettingsClick()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.song_history)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        isMoreMenuExpanded = false
                        onSongHistoryClick()
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMainTopBar() {
    MainTopBar()
}
