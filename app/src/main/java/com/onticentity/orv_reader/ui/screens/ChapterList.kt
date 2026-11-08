package com.onticentity.orv_reader.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ChapterList(files: List<String>, onSelect: (String) -> Unit) {
    LazyColumn {
        if (files.isEmpty()) {
            item { Text("No chapters found.") }
        }

        item {
            Text("${files.size} Chapters")
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
