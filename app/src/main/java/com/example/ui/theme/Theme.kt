package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val StandByColorScheme = darkColorScheme(
    primary = AppleMint,
    onPrimary = Color.Black,
    secondary = AppleBlue,
    onSecondary = Color.White,
    tertiary = AppleOrange,
    background = StandByBlack,
    onBackground = AppleTextPrimary,
    surface = StandByDarkSurface,
    onSurface = AppleTextPrimary,
    surfaceVariant = StandByCardBg,
    onSurfaceVariant = AppleTextSecondary
)

@Composable
fun StandByTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StandByColorScheme,
        typography = Typography,
        content = content
    )
}
