package com.onticentity.orv_reader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.onticentity.orv_reader.data.ThemeMode

private val DarkReaderColorScheme = darkColorScheme(
    primary = ReaderAccent,
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
    surfaceTint = ReaderAccent,
    outline = ReaderOutline,
    outlineVariant = ReaderSubtleOutline
)

private val LightReaderColorScheme = lightColorScheme(
    primary = Color(0xFF625E50),
    onPrimary = Color(0xFFF3F0E7),
    secondary = Color(0xFF69685F),
    onSecondary = Color(0xFFF3F0E7),

    background = Color(0xFFF3F0E7),
    onBackground = Color(0xFF34352F),
    surface = Color(0xFFF3F0E7),
    onSurface = Color(0xFF34352F),

    surfaceVariant = Color(0xFFE8E5DB),
    onSurfaceVariant = Color(0xFF69685F),
    surfaceContainer = Color(0xFFE8E5DB),
    surfaceContainerLowest = Color(0xFFF3F0E7),

    surfaceTint = Color(0xFF625E50),
    outline = Color(0xFF8D8C80),
    outlineVariant = Color(0xFFCBC7BB)
)

@Composable
fun ORVReaderTheme(
    mode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {

    var darkTheme = when (mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkReaderColorScheme
        else LightReaderColorScheme,
        typography = Typography,
        content = content
    )
}
