package com.onticentity.orv_reader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ReaderColorScheme = darkColorScheme(
    primary = ReaderText,
    onPrimary = ReaderBackground,
    secondary = ReaderSecondaryText,
    onSecondary = ReaderBackground,
    background = ReaderBackground,
    onBackground = ReaderText,
    surface = ReaderBackground,
    onSurface = ReaderText,
    surfaceVariant = ReaderSurface,
    onSurfaceVariant = ReaderSecondaryText,
    surfaceContainer = ReaderSurface,
    surfaceContainerLowest = ReaderHeader,
    surfaceTint = ReaderText,
    outline = ReaderOutline,
    outlineVariant = ReaderSubtleOutline
)

@Composable
fun ORVReaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ReaderColorScheme,
        typography = Typography,
        content = content
    )
}
