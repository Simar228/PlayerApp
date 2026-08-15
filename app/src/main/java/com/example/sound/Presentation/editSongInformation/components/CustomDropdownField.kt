package com.example.sound.Presentation.editSongInformation.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableGenreDropdown(
    label: String,
    currentValue: String,
    suggestions: List<String>,
    onValueChange: (String) -> Unit
) {
    val localFocusManager = LocalFocusManager.current
    var focusState by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(currentValue, suggestions) {
        if (currentValue.isEmpty()) {
            suggestions
        } else {
            suggestions.filter { it.contains(currentValue, ignoreCase = true) }
        }
    }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = currentValue,
                selection = TextRange(currentValue.length)
            )
        )
    }

    LaunchedEffect(focusState) {
        expanded = focusState
    }

    LaunchedEffect(currentValue) {
        if (currentValue != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = currentValue,
                selection = TextRange(currentValue.length)
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                modifier = Modifier.weight(1f),
                expanded = expanded,
                onExpandedChange = {
                }
            ) {
                OutlinedTextField(
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            localFocusManager.clearFocus()
                        }
                    ),
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onValueChange(newValue.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                        .onFocusChanged { _focusState ->
                            focusState = _focusState.isFocused
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorContainerColor = MaterialTheme.colorScheme.error,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    )
                )


                if (filteredSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {  }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(text = suggestion) },
                                onClick = {
                                    onValueChange(suggestion)
                                    expanded = false

                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
            if (focusState) {
                Icon(
                    if (expanded) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = "Очистить",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { expanded = !expanded }
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Интерактивный выбор жанра")
@Composable
private fun EditableGenreDropdownPreview() {
    // Внутри Preview используем локальное состояние, имитируя поведение ViewModel
    var selectedGenre by remember { mutableStateOf("") }

    // Список тестовых данных, которые как будто пришли из базы данных Room
    val mockGenres = listOf(
        "Рок",
        "Поп",
        "Джаз",
        "Хип-хоп",
        "Классика",
        "Электроника",
        "Метал"
    )

    MaterialTheme {
        // Surface обеспечивает правильный фон для элементов Material 3
        Surface(color = MaterialTheme.colorScheme.background) {
            EditableGenreDropdown(
                label = "Жанр песни",
                currentValue = selectedGenre,
                suggestions = mockGenres,
                onValueChange = { newValue ->
                    selectedGenre = newValue
                }
            )
        }
    }
}