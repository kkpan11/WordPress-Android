package org.wordpress.android.ui.compose.styles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DashboardCardTypography {
    val title: TextStyle
        @Composable
        get() = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface.copy(alpha = ContentAlpha.high)
        )

    val smallTitle: TextStyle
        @Composable
        get() = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.onSurface.copy(alpha = ContentAlpha.high)
        )

    val subTitle: TextStyle
        @Composable
        get() = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Normal,
            color = colors.onSurface.copy(alpha = ContentAlpha.high)
        )

    val detailText: TextStyle
        @Composable
        get() = MaterialTheme.typography.bodyMedium.copy(
            color = colors.onSurface.copy(alpha = ContentAlpha.medium)
        )

    val largeText: TextStyle
        @Composable
        get() = MaterialTheme.typography.headlineMedium.copy(
            color = colors.onSurface.copy(alpha = ContentAlpha.high)
        )

    val footerCTA: TextStyle
        @Composable
        get() = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Medium,
            color = colors.primary
        )

    val standaloneText: TextStyle
        @Composable
        get() = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Medium,
            color = colors.onSurface.copy(alpha = ContentAlpha.high)
        )
}

@Preview
@Composable
fun DashboardCardTypographyPreview() {
    val padding = Modifier.padding(8.dp)

    Column {
        Text(
            text = "Title",
            style = DashboardCardTypography.title,
            modifier = padding
        )
        Text(
            text = "subTitle",
            style = DashboardCardTypography.subTitle,
            modifier = padding
        )
        Text(
            text = "detailText",
            style = DashboardCardTypography.detailText,
            modifier = padding
        )
        Text(
            text = "largeText",
            style = DashboardCardTypography.largeText,
            modifier = padding
        )
        Text(
            text = "footerCTA",
            style = DashboardCardTypography.footerCTA,
            modifier = padding
        )
        Text(
            text = "standaloneText",
            style = DashboardCardTypography.standaloneText,
            modifier = padding
        )
    }
}
