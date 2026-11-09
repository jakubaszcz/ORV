package com.onticentity.orv_reader.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.onticentity.orv_reader.R
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onticentity.orv_reader.ui.theme.ORVReaderTheme
import com.onticentity.orv_reader.data.chapterFileOrder
import com.onticentity.orv_reader.data.EpisodeGroup

private fun chapterLabel(file: String): String =
    if (file == "prologue.json") "Prologue"
    else "Chapter ${file.removePrefix("ch_").removeSuffix(".json")}"

@Composable
fun ChapterList(
    files: List<String>,
    onSelect: (String) -> Unit,
    savedChapter: String? = null,
    onResume: () -> Unit = {},
    episodes: List<EpisodeGroup> = emptyList()
) {
    val listState = rememberLazyListState()
    val background = MaterialTheme.colorScheme.background
    val cardShape = RoundedCornerShape(8.dp)
    val cardColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    val cardBorder = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    val orderedFiles = remember(files) { files.sortedBy(::chapterFileOrder) }

    val groupedFiles = remember(episodes) { episodes.flatMap { it.files }.toSet() }
    val standaloneFiles = orderedFiles.filter { it !in groupedFiles }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Box(Modifier.fillMaxSize()) {
            // Anchored to the first list item: moves away at the same speed as the content.
            if (listState.firstVisibleItemIndex == 0) {
                Box(Modifier.fillMaxWidth().height(480.dp).offset {
                    IntOffset(0, -listState.firstVisibleItemScrollOffset)
                }) {
                    Image(
                        painter = painterResource(R.drawable.reader_portrait),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(Modifier.matchParentSize().background(Brush.verticalGradient(
                        0f to background.copy(alpha = 0f),
                        0.25f to background.copy(alpha = 0f),
                        0.55f to background.copy(alpha = 0.45f),
                        0.82f to background.copy(alpha = 0.92f),
                        1f to background
                    )))
                    Image(
                        painter = painterResource(R.drawable.orv_korean_logo),
                        contentDescription = "Omniscient Reader’s Viewpoint",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.align(Alignment.TopEnd)
                            .padding(top = 24.dp, end = 8.dp)
                            .size(132.dp)
                            .graphicsLayer { blendMode = BlendMode.Screen }
                    )
                }
            }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header") {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 310.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Omniscient Reader’s\nViewpoint",
                        fontFamily = FontFamily.Serif,
                        fontSize = 32.sp, lineHeight = 35.sp,
                        fontWeight = FontWeight.Medium)
                    Text("${files.count { it != "prologue.json" }} chapters · ${episodes.count { it.kind == "episode" }} episodes", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }

            if (savedChapter != null && savedChapter in files) {
                item(key = "resume") {
                    Surface(
                        onClick = onResume,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = cardShape,
                        color = cardColor,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = cardBorder
                    ) {
                        Row(
                            modifier = Modifier.heightIn(min = 64.dp)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("Continue reading", fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(chapterLabel(savedChapter), fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium)
                            }
                            Text("›", fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }

            item(key = "chapters-title") {
                Text(
                    "Chapters and episodes",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )
            }

            if (files.isEmpty()) {
                item(key = "empty") { Text("No chapters found.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            items(standaloneFiles, key = { it }) { file ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    color = cardColor,
                    border = cardBorder
                ) {
                    ChapterRow(file, savedChapter == file, onSelect)
                }
            }

            items(episodes, key = { "episode-${it.id}" }) { episode ->
                // Null follows the saved chapter, including when progress loads asynchronously.
                // A manual choice takes priority and survives scrolling and rotation.
                var expansionOverride by rememberSaveable(episode.id) {
                    mutableStateOf<Boolean?>(null)
                }
                val expanded = expansionOverride ?: (savedChapter in episode.files)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    color = cardColor,
                    border = cardBorder
                ) {
                    Column(modifier = Modifier.animateContentSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                                .clickable(
                                    role = Role.Button,
                                    onClickLabel = if (expanded) "Collapse chapters" else "Expand chapters",
                                    onClick = { expansionOverride = !expanded }
                                )
                                .heightIn(min = 64.dp)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text("${episode.label} · ${episode.files.size} chapters",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                Text(episode.title, fontSize = 15.sp, lineHeight = 21.sp,
                                    fontWeight = FontWeight.Medium)
                            }
                            Text(if (expanded) "−" else "+", fontSize = 18.sp,
                                modifier = Modifier.padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.primary)
                        }
                        if (expanded) {
                            episode.files.forEach { file ->
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp),
                                    thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                ChapterRow(file, savedChapter == file, onSelect)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun ChapterRow(file: String, saved: Boolean, onSelect: (String) -> Unit) {
    Surface(
        onClick = { onSelect(file) },
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(chapterLabel(file), fontSize = 14.sp)
                if (saved) Text("Saved position", fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary)
            }
            Text("›", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChapterListPreview() {
    ORVReaderTheme {
        ChapterList(
            files = listOf("prologue.json") +
                    (1..10).map { "ch_$it.json" },
            savedChapter = "ch_10.json",
            onSelect = {},
            onResume = {},
            episodes = listOf(
                EpisodeGroup("episode", 1, "Starting the Paid Service", (1..5).map { "ch_$it.json" }),
                EpisodeGroup("episode", 2, "Protagonist", (6..10).map { "ch_$it.json" })
            )
        )
    }
}
