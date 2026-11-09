package com.onticentity.orv_reader

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.onticentity.orv_reader.data.listChapterFiles
import com.onticentity.orv_reader.data.loadEpisodeGroups
import com.onticentity.orv_reader.ui.screens.ChapterList
import com.onticentity.orv_reader.ui.screens.ChapterScreen
import com.onticentity.orv_reader.data.ReadingProgress
import com.onticentity.orv_reader.data.loadChapter
import com.onticentity.orv_reader.data.saveChapter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ReaderApp() {

    val context = LocalContext.current

    val files = remember(context) { listChapterFiles(context) }
    val episodes = remember(context, files) { loadEpisodeGroups(context, files) }

    var selectedChapter by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var targetParagraphId by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var startAtEnd by rememberSaveable {
        mutableStateOf(false)
    }

    BackHandler(enabled = selectedChapter != null) {
        selectedChapter = null
    }

    var savedProgress by remember { mutableStateOf<ReadingProgress?>(null) }

    LaunchedEffect(selectedChapter) {
        if (selectedChapter == null) {
            try {
                savedProgress = loadChapter(context)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to load saved progress", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val file = selectedChapter

    if (file == null) {
        ChapterList(
            files = files,
            episodes = episodes,
            savedChapter = savedProgress?.chapterFile,
            onResume = {
                savedProgress?.takeIf { it.chapterFile in files }?.let { progress ->
                    targetParagraphId = progress.paragraphId
                    startAtEnd = false
                    selectedChapter = progress.chapterFile
                }
            },
            onSelect = {
                targetParagraphId = null
                startAtEnd = false
                selectedChapter = it
            }
        )
    } else {

        val index = files.indexOf(file)


        key(file) {
            ChapterScreen(
                file = file,
                episodeLabel = episodes.firstOrNull { file in it.files }?.label,
                targetParagraphId = targetParagraphId,
                startAtEnd = startAtEnd,
                canGoNext = index >= 0 && index < files.lastIndex,
                canGoPrevious = index > 0,
                onSaveProgress = { paragraph ->
                    scope.launch {
                        try {
                            saveChapter(context, file, paragraph)

                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Unable to save progress",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
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
