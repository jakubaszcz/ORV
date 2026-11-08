package com.onticentity.orv_reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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
    var selectedChapter by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    BackHandler(enabled = selectedChapter != null) {
        selectedChapter = null
    }

    var file = selectedChapter

    if (file == null) {
        ChapterList(onSelect = { selectedChapter = it})
    } else {
        ChapterScreen(file, onBack = { selectedChapter = null })
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
            Button(onClick = { onSelect(file) }) {
                Text(file.removeSuffix(".json"))
            }
        }
    }
}

@Composable
fun ChapterScreen(file: String, onBack: () -> Unit) {
    val context = LocalContext.current
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

    Column(Modifier.fillMaxSize()) {
        Button(onClick = onBack) {
            Text("Back")
        }

        if (message.isNotEmpty()) {
            Text(message)
        }

        LazyColumn(Modifier.weight(1f)) {
            items(blocks) { block ->
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