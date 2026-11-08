package com.onticentity.orv_reader.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

data class ReadingProgress(
    val chapterFile: String,
    val paragraphId: String?
)

private val Context.readingDataStore by preferencesDataStore(
    name = "reading_progress"
)

private val chapterKey = stringPreferencesKey("last_chapter")
private val paragraphKey = stringPreferencesKey("last_paragraph")

suspend fun saveChapter(context: Context, file: String, paragraph: String) {
    context.applicationContext.readingDataStore.edit { preferences ->
        preferences[chapterKey] = file
        preferences[paragraphKey] = paragraph
    }
}

suspend fun loadChapter(context: Context): ReadingProgress? {
    val preferences = context.applicationContext
        .readingDataStore.data.first()

    val chapterFile = preferences[chapterKey] ?: return null

    return ReadingProgress(
        chapterFile = chapterFile,
        paragraphId = preferences[paragraphKey]
    )
}