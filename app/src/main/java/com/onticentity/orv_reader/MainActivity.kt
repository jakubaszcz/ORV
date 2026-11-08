package com.onticentity.orv_reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReaderApp()
        }
    }
}

@Composable
fun ReaderApp() {

    var context = LocalContext.current

    val files = remember(context) {
        context.assets.list("chapters")
            .orEmpty()
            .filter { it.matches(Regex("""ch_\d+\.json""")) }
            .sortedBy {
                it.removePrefix("ch_")
                    .removeSuffix(".json")
                    .toLongOrNull() ?: Long.MAX_VALUE
            }
    }

    var selectedChapter by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var startAtEnd by rememberSaveable {
        mutableStateOf(false)
    }

    BackHandler(enabled = selectedChapter != null) {
        selectedChapter = null
    }

    var file = selectedChapter

    if (file == null) {
        ChapterList(onSelect = {
            startAtEnd = false
            selectedChapter = it
        })
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

@Composable
fun ChapterList(onSelect: (String) -> Unit) {
    val context = LocalContext.current

    val files = remember(context) {
        context.assets.list("chapters")
            .orEmpty()
            .filter { it.matches(Regex("""ch_\d+\.json""")) }
            .sortedBy {
                it.removePrefix("ch_")
                    .removeSuffix(".json")
                    .toLongOrNull() ?: Long.MAX_VALUE
            }
    }

    LazyColumn {
        if (files.isEmpty()) {
            item { Text("No chapters found.") }
        }

        items(files) { file ->
            val chapter = file
                .removePrefix("ch_")
                .removeSuffix(".json")
                .toIntOrNull()

            Button(onClick = { onSelect(file) }) {
                Text("Chapter ${chapter ?: "?"}")
            }
        }
    }
}

@Composable
fun ChapterScreen(
    file: String,
    startAtEnd: Boolean,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var blocks by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var message by remember { mutableStateOf("Loading...") }

    LaunchedEffect(file) {
        try {
            blocks = withContext(Dispatchers.IO) {
                val text = context.assets.open("chapters/$file")
                    .bufferedReader()
                    .use { it.readText() }

                val array = JSONArray(text)

                List(array.length()) { array.getJSONObject(it) }
            }

            message = ""
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            message = "Unable to read chapter."
        }
    }

    val gestures = chapterGestures(
        listState = listState,
        canGoNext = canGoNext && blocks.isNotEmpty(),
        canGoPrevious = canGoPrevious && blocks.isNotEmpty(),
        onNext = onNext,
        onPrevious = onPrevious,
        onBack = onBack
    )

    Column(Modifier.fillMaxSize()) {

        if (message.isNotEmpty()) {
            Text(message)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(gestures)
        ) {
            items(blocks, key = { it.getString("id") }) { block ->
                DisplayBlock(block)
            }
        }
    }
}

@Composable
fun DisplayBlock(block: JSONObject) {
    when (block.getString("type")) {
        "paragraph", "heading" -> {
            Text(block.getString("text"))
        }

        "box" -> {
            val children = block.getJSONArray("blocks")

            Column {
                for (index in 0 until children.length()) {
                    DisplayBlock(children.getJSONObject(index))
                }
            }
        }
    }
}

@Composable
fun chapterGestures(
    listState: LazyListState,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onBack: () -> Unit
): Modifier {
    val scope = rememberCoroutineScope()
    val threshold = with(LocalDensity.current) { 80.dp.toPx() }

    val latestNext by rememberUpdatedState(onNext)
    val latestPrevious by rememberUpdatedState(onPrevious)
    val latestBack by rememberUpdatedState(onBack)

    var touching by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableStateOf(0f) }
    var changeJob by remember { mutableStateOf<Job?>(null) }

    fun cancelPull() {
        changeJob?.cancel()
        changeJob = null
        pullDistance = 0f
    }

    val connection = remember(
        listState,
        threshold,
        canGoNext,
        canGoPrevious
    ) {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (
                    source == NestedScrollSource.UserInput &&
                    pullDistance * available.y < 0f
                ) {
                    cancelPull()
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!touching || source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }

                val next = available.y < 0f &&
                        !listState.canScrollForward &&
                        canGoNext

                val previous = available.y > 0f &&
                        !listState.canScrollBackward &&
                        canGoPrevious

                if (!next && !previous) {
                    return Offset.Zero
                }

                pullDistance += available.y

                if (abs(pullDistance) >= threshold && changeJob == null) {
                    val goNext = next

                    changeJob = scope.launch {
                        delay(1_000L)

                        val stillAtEdge = if (goNext) {
                            !listState.canScrollForward
                        } else {
                            !listState.canScrollBackward
                        }

                        if (touching && stillAtEdge) {
                            // Only one chapter change per gesture.
                            touching = false
                            pullDistance = 0f
                            changeJob = null

                            if (goNext) {
                                latestNext()
                            } else {
                                latestPrevious()
                            }
                        }
                    }
                }

                // Consume only the movement beyond the edge.
                return Offset(0f, available.y)
            }
        }
    }

    return Modifier
        .nestedScroll(connection)
        .pointerInput(Unit) {
            // Observe the finger without consuming vertical scrolling.
            awaitEachGesture {
                awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial
                )

                cancelPull()
                touching = true

                try {
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                    } while (event.changes.any { it.pressed })
                } finally {
                    touching = false
                    cancelPull()
                }
            }
        }
        .pointerInput(threshold) {
            var horizontalDistance = 0f

            detectHorizontalDragGestures(
                onDragStart = {
                    horizontalDistance = 0f
                    touching = false
                    cancelPull()
                },
                onHorizontalDrag = { _, distance ->
                    horizontalDistance += distance
                },
                onDragEnd = {
                    if (abs(horizontalDistance) >= threshold) {
                        latestBack()
                    }
                    horizontalDistance = 0f
                },
                onDragCancel = {
                    horizontalDistance = 0f
                }
            )
        }
}