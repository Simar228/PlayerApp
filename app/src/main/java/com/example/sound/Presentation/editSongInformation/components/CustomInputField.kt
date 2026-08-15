package com.example.sound.Presentation.editSongInformation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, start = 12.dp, end = 12.dp)) {
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
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorContainerColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                )
            )
            if (value.isNotEmpty()) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Очистить",
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onValueChange("") }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCustomInputField(){
    CustomInputField(
        label = "label",
        value = "CupSize",
        onValueChange = {},
    )
}