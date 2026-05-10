package com.example.ngepet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = Color.White,
    primaryContainer = Green50,
    onPrimaryContainer = Green800,
    secondary = Pink400,
    onSecondary = Color.White,
    secondaryContainer = Pink50,
    onSecondaryContainer = Pink800,
    error = Danger,
    background = SurfaceWarm,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = CardSoft,
    onSurfaceVariant = Muted
)

@Composable
fun NgepetTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
