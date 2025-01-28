package org.wordpress.android.ui.qrcodeauth.compose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import org.wordpress.android.ui.compose.unit.FontSize
import org.wordpress.android.ui.compose.unit.Margin

@Composable
fun Subtitle(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        fontSize = FontSize.Large.value,
        color = color,
        modifier = modifier
            .wrapContentSize()
            .padding(
                start = Margin.ExtraExtraMediumLarge.value,
                end = Margin.ExtraExtraMediumLarge.value,
                top = Margin.ExtraLarge.value + Margin.Small.value,
                bottom = Margin.Medium.value
            )
    )
}
