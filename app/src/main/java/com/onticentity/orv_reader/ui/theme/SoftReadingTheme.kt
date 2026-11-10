package com.onticentity.orv_reader.ui.theme

import androidx.compose.runtime.Composable

// Kept as an alias for existing callers; the whole app now shares this palette.
@Composable
fun SoftReadingTheme(content: @Composable () -> Unit) {
    content()
}
