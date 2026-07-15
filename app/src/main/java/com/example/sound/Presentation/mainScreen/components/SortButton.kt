package com.example.sound.Presentation.mainScreen.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sound.R


@Composable
fun SortButton(
    isUp: Boolean,
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isUp) -90f else 90f,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "ArrowRotation"
    )


    Button(
        border = BorderStroke(
            width = 1.dp,
            color = Color.LightGray
        ),
        modifier = Modifier
            .defaultMinSize(
                minWidth = 0.dp,
                minHeight = 0.dp
            )
            .animateContentSize(),
        contentPadding = PaddingValues(
            horizontal = 10.dp,
            vertical = 4.dp
        ),
        onClick = {
            onClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
        ),

    ) {
        Text(text)
        if (isActive) {
            Icon(
                painter = painterResource(R.drawable.outline_arrow_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 7.dp)
                    .size(25.dp)
                    .rotate(rotationAngle)


            )
        }
    }
}

@Composable
@Preview
private fun PreviewSortButton() {
    SortButton(
        true,
        "Артист",
        true,
    ) { }
}