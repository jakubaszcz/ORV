package com.onticentity.orv_reader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import com.onticentity.orv_reader.ui.theme.SoftReadingTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.onticentity.orv_reader.ui.theme.Charter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import com.onticentity.orv_reader.ui.gestures.ChapterPullState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.onticentity.orv_reader.data.readChapterBlocks
import com.onticentity.orv_reader.ui.components.DisplayBlock
import com.onticentity.orv_reader.ui.components.ReadingFooter
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
    onPrevious: () -> Unit,
    episodeLabel: String? = null
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
        blocks.firstOrNull {
            it.optString("type") == "heading"
        }?.optString("text").orEmpty()
    }

    val chapterLabel = if (file == "prologue.json") "Prologue"
        else "Chapter ${file.removePrefix("ch_").removeSuffix(".json")}"
    val chapterTitle = headerTitle.substringAfter(":", headerTitle).trim()

    val pull = remember(file) { ChapterPullState() }
    val pullOffset by animateFloatAsState(pull.distance, spring(stiffness = 700f), label = "chapterPull")
    val gestures = chapterGestures(
        listState = listState,
        feedback = pull,
        canGoNext = canGoNext && blocks.isNotEmpty(),
        canGoPrevious = canGoPrevious && blocks.isNotEmpty(),
        onNext = onNext,
        onPrevious = onPrevious,
        onBack = onBack
    )

    SoftReadingTheme {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier.fillMaxSize().displayCutoutPadding().navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = if (file == "prologue.json" || episodeLabel == null) chapterLabel
                        else "$episodeLabel · $chapterLabel",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 12.dp)
                )
            }
    
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding( 20.dp)
                )
            }
    
            Box(
                Modifier.weight(1f).widthIn(max = 680.dp).fillMaxWidth().clipToBounds()
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 26.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                        .graphicsLayer { translationY = pullOffset }
                        .then(gestures)
                ) {
                    items(blocks, key = { it.getString("id") }) { block ->
                        if (block === blocks.firstOrNull() && block.optString("type") == "heading") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(chapterTitle, fontFamily = Charter, fontSize = 29.sp,
                                    lineHeight = 36.sp, fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center)
                                HorizontalDivider(Modifier.width(32.dp), thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            DisplayBlock(block, onSaveProgress = onSaveProgress)
                        }
                    }
                }
                if (pull.distance != 0f) {
                    val previous = pull.distance > 0f
                    Column(
                        modifier = Modifier
                            .align(if (previous) Alignment.TopCenter else Alignment.BottomCenter)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (previous && pull.counting) {
                            Text("Previous chapter",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        CircularProgressIndicator(
                            progress = { pull.progress },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                        if (!previous && pull.counting) {
                            Text("Next chapter",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Box(Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                ReadingFooter()
            }
        }
    }
}
}
