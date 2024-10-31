package org.wordpress.android.ui.compose.components.menu.dropdown

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.wordpress.android.ui.compose.theme.AppColor

object JetpackDropdownMenuColors {
    @Composable
    fun itemContentColor(): Color = if (isSystemInDarkTheme()) {
        AppColor.White
    } else {
        AppColor.Black
    }

    @Composable
    fun itemBackgroundColor(): Color = if (isSystemInDarkTheme()) {
        AppColor.DarkGray90
    } else {
        AppColor.White
    }

    @Composable
    fun itemDividerColor(): Color = if (isSystemInDarkTheme()) {
        AppColor.Gray50
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }
}
