package com.onticentity.orv_reader

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.onticentity.orv_reader.data.listChapterFiles
import com.onticentity.orv_reader.ui.screens.ChapterList
import com.onticentity.orv_reader.ui.screens.ChapterScreen

@Composable
fun ReaderApp() {

    val context = LocalContext.current

    val files = remember(context) { listChapterFiles(context) }

    var selectedChapter by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var startAtEnd by rememberSaveable {
        mutableStateOf(false)
    }

    BackHandler(enabled = selectedChapter != null) {
        selectedChapter = null
    }

    val file = selectedChapter

    if (file == null) {
        ChapterList(
            onSelect = {
                startAtEnd = false
                selectedChapter = it
            },
            files = files
        )
    } else {

        val index = files.indexOf(file)


        key(file) {
            ChapterScreen(
                file = file,
                startAtEnd = startAtEnd,
                canGoNext = index >= 0 && index < files.lastIndex,
                canGoPrevious = index > 0,
                onBack = { selectedChapter = null },
                onNext = {
                    files.getOrNull(index + 1)?.let {
                        startAtEnd = false
                        selectedChapter = it
                    }
                },
                onPrevious = {
                    files.getOrNull(index - 1)?.let {
                        startAtEnd = true
                        selectedChapter = it
                    }
                }
            )
        }
    }
}
