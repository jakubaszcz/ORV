package com.onticentity.orv_reader.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    DARK("Dark"),
    LIGHT("Light");

    fun next(): ThemeMode = when (this) {
        DARK -> LIGHT
        LIGHT -> SYSTEM
        SYSTEM -> DARK
    }
}

private val Context.appearanceStore by preferencesDataStore(name = "appearance")
private val themeKey = stringPreferencesKey("theme_mode")

fun observeTheme(context: Context) = context.applicationContext.appearanceStore.data
    .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
    .map { preferences -> ThemeMode.entries.firstOrNull { it.name == preferences[themeKey] } ?: ThemeMode.DARK }

suspend fun saveTheme(context: Context, mode: ThemeMode) {
    context.applicationContext.appearanceStore.edit { it[themeKey] = mode.name }
}
