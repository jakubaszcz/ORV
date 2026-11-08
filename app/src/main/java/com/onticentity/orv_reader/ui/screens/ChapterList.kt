package com.onticentity.orv_reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChapterList(files: List<String>, onSelect: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0908),
        contentColor = Color(0xFFF2F2F2)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("${files.size} Chapters", fontSize = 22.sp)
            }

            if (files.isEmpty()) {
                item { Text("No chapters found.") }
            }

            items(files, key = { it }) { file ->
                val chapter = file
                    .removePrefix("ch_")
                    .removeSuffix(".json")
                    .toIntOrNull()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .background(Color(0xFF202020))
                        .clickable(onClickLabel = "Open chapter") { onSelect(file) }
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("Chapter ${chapter ?: "?"}", fontSize = 18.sp)
                }
            }
        }
    }
}
