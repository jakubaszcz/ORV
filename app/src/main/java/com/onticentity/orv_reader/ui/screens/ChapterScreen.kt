package com.onticentity.orv_reader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.onticentity.orv_reader.data.readChapterBlocks
import com.onticentity.orv_reader.ui.components.DisplayBlock
import com.onticentity.orv_reader.ui.gestures.chapterGestures
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun ChapterScreen(
    file: String,
    targetParagraphId: String?,
    startAtEnd: Boolean,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    onSaveProgress: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var blocks by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var message by remember { mutableStateOf("Loading...") }

    LaunchedEffect(blocks, targetParagraphId) {
        val paragraphId = targetParagraphId

        if (blocks.isNotEmpty() && paragraphId != null) {
            val index = blocks.indexOfFirst {
                it.optString("id") == paragraphId
            }

            if (index >= 0) {
                listState.scrollToItem(index)
            }
        }
    }

    LaunchedEffect(file) {
        try {
            blocks = withContext(Dispatchers.IO) {
                readChapterBlocks(context, file)
            }

            message = ""
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            message = "Unable to read chapter."
        }
    }

    val headerTitle = remember(file, blocks) {
        val title = blocks.firstOrNull { it.optString("type") == "heading" }
            ?.optString("text").orEmpty()
        formatChapterHeader(file, title)
    }

    val gestures = chapterGestures(
        listState = listState,
        canGoNext = canGoNext && blocks.isNotEmpty(),
        canGoPrevious = canGoPrevious && blocks.isNotEmpty(),
        onNext = onNext,
        onPrevious = onPrevious,
        onBack = onBack
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = headerTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 25.dp,
                        bottom = 8.dp
                    )
                )
            }
    
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding( 20.dp)
                )
            }
    
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(gestures)
            ) {
                items(blocks, key = { it.getString("id") }) { block ->
                    DisplayBlock(block, onSaveProgress = onSaveProgress)
                }
            }
        }
    }
}


private fun formatChapterHeader(file: String, originalTitle: String): String {
    val chapter = file.removePrefix("ch_").removeSuffix(".json")
    var title = originalTitle
        .replace(Regex("""^Ch\s+\d+\s*:\s*""", RegexOption.IGNORE_CASE), "")
        .replace(Regex("""^Ep\.\s*\d+\s*[-\u2013\u2014]\s*""", RegexOption.IGNORE_CASE), "")
        .trim()

    val part = Regex(""",\s*([IVXLCDM]+)\s*$""").find(title)
    if (part != null) {
        val values = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50,
            'C' to 100, 'D' to 500, 'M' to 1000)
        var number = 0
        var previous = 0
        for (character in part.groupValues[1].reversed()) {
            val value = values.getValue(character)
            number += if (value < previous) -value else value
            previous = maxOf(previous, value)
        }
        title = title.substring(0, part.range.first).trimEnd() + " ($number)"
    }

    return if (title.isBlank()) "Chapitre $chapter" else "Chapitre $chapter - $title"
}
