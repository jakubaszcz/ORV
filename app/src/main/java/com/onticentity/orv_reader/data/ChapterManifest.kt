package com.onticentity.orv_reader.data

import android.content.Context
import android.util.Log
import org.json.JSONObject

data class EpisodeGroup(
    val kind: String,
    val number: Int,
    val title: String,
    val files: List<String>
) {
    val id: String get() = "$kind-$number"
    val label: String get() = "${if (kind == "epilogue") "Epilogue" else "Episode"} $number"
}

// A missing or invalid manifest must never make saved chapters inaccessible.
fun loadEpisodeGroups(context: Context, files: List<String>): List<EpisodeGroup> {
    return try {
        val text = context.assets.open("chapters/manifest.json").bufferedReader().use { it.readText() }
        parseEpisodeGroups(text, files)
    } catch (error: Exception) {
        Log.w("ChapterManifest", "Unable to load episode groups; showing chapter list", error)
        emptyList()
    }
}

internal fun parseEpisodeGroups(text: String, files: List<String>): List<EpisodeGroup> {
    val manifest = JSONObject(text)
    require(manifest.getInt("version") == 1) { "Unsupported manifest version" }
    val prologue = manifest.getJSONObject("prologue").getString("file")
    require(prologue == "prologue.json" && prologue in files)
    val episodes = manifest.getJSONArray("episodes")
    val groups = List(episodes.length()) { index ->
        val episode = episodes.getJSONObject(index)
        val kind = episode.optString("kind", "episode")
        require(kind == "episode" || kind == "epilogue")
        val number = episode.getInt("number")
        val title = episode.getString("title")
        require(number > 0 && title.isNotBlank())
        val chapters = episode.getJSONArray("chapters")
        require(chapters.length() > 0)
        val chapterFiles = List(chapters.length()) { chapterIndex ->
            val chapter = chapters.getJSONObject(chapterIndex)
            val chapterNumber = chapter.getInt("number")
            val file = chapter.getString("file")
            require(chapterNumber > 0 && file == "ch_$chapterNumber.json" && file in files)
            file
        }.sortedBy(::chapterFileOrder)
        EpisodeGroup(kind, number, title, chapterFiles)
    }.sortedWith(compareBy<EpisodeGroup> { it.kind == "epilogue" }.thenBy { it.number })
    require(groups.map { it.id }.distinct().size == groups.size)
    val orderedFiles = listOf(prologue) + groups.flatMap { it.files }
    require(orderedFiles == files.sortedBy(::chapterFileOrder)) {
        "Manifest must contain every chapter exactly once, in reading order"
    }
    return groups
}
