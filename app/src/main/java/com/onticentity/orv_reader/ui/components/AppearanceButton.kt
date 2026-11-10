package com.onticentity.orv_reader.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.onticentity.orv_reader.R
import com.onticentity.orv_reader.data.ThemeMode
@Composable
fun AppearanceButton(mode: ThemeMode, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant

    val icon = when (mode) {
        ThemeMode.LIGHT -> R.drawable.ic_theme_light
        ThemeMode.DARK -> R.drawable.ic_theme_dark
        ThemeMode.SYSTEM -> R.drawable.ic_theme_system
    }

    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(icon),
            contentDescription =
                "Appearance: ${mode.label}. Switch to ${mode.next().label}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
    }
}
