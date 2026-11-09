package com.onticentity.orv_reader.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onticentity.orv_reader.ui.theme.ORVReaderTheme
import com.onticentity.orv_reader.data.chapterFileOrder

private fun chapterLabel(file: String): String =
    if (file == "prologue.json") "Prologue"
    else "Chapitre ${file.removePrefix("ch_").removeSuffix(".json")}"

@Composable
fun ChapterList(
    files: List<String>,
    onSelect: (String) -> Unit,
    savedChapter: String? = null,
    onResume: () -> Unit = {}
) {
    val orderedFiles = remember(files) { files.sortedBy(::chapterFileOrder) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "header") {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("ORV Reader", fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                    Text("${files.size} chapters", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }

            if (savedChapter != null && savedChapter in files) {
                item(key = "resume") {
                    Surface(
                        onClick = onResume,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {



                                Text(
                                    "CONTINUE READING",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    chapterLabel(savedChapter),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text("Resume saved progress", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                            Text("\u2192", fontSize = 24.sp, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }

            item(key = "chapters-title") {
                Text(
                    "Chapters",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (files.isEmpty()) {
                item(key = "empty") { Text("No chapters found.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            items(orderedFiles, key = { it }) { file ->
                Surface(
                    onClick = { onSelect(file) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Row(
                        modifier = Modifier.heightIn(min = 64.dp).padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(chapterLabel(file), fontSize = 16.sp, modifier = Modifier.weight(1f))
                        Text("\u203A", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ChapterListPreview() {
    ORVReaderTheme {
        ChapterList(
            files = listOf("prologue.json") +
                    (1..550).map { "ch_$it.json" },
            savedChapter = "ch_10.json",
            onSelect = {},
            onResume = {}
        )
    }
}
