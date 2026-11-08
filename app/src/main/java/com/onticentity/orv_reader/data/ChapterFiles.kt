package com.onticentity.orv_reader.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// These functions read assets synchronously. Read chapter contents on Dispatchers.IO.
fun listChapterFiles(context: Context): List<String> {
    return context.assets.list("chapters")
        .orEmpty()
        .filter { it.matches(Regex("""ch_\d+\.json""")) }
        .sortedBy {
            it.removePrefix("ch_")
                .removeSuffix(".json")
                .toLongOrNull() ?: Long.MAX_VALUE
        }
}

fun readChapterBlocks(context: Context, file: String): List<JSONObject> {
    val text = context.assets.open("chapters/$file")
        .bufferedReader()
        .use { it.readText() }

    val array = JSONArray(text)
    return List(array.length()) { array.getJSONObject(it) }
}
