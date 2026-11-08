package com.onticentity.orv_reader.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

@Composable
fun DisplayBlock(block: JSONObject, onSaveProgress: (String) -> Unit) {
    when (block.getString("type")) {
        "paragraph" -> {
            Box(modifier = Modifier.padding(4.dp).combinedClickable(
                onClick = {},
                onDoubleClick = { onSaveProgress(block.getString("id")) }
            )) {
                Text(
                    text = block.getString("text"),
                    fontSize = 14.sp
                )
            }
        }

        "heading" -> {
            val level = block.optInt("level", 2)

            Box(modifier = Modifier.padding(4.dp)) {
                Text(
                    text = block.getString("text"),
                    fontSize = when (level) {
                        1 -> 28.sp
                        2 -> 24.sp
                        3 -> 20.sp
                        else -> 18.sp
                    }
                )
            }
        }

        "box" -> {
            val children = block.getJSONArray("blocks")

            Column {
                for (index in 0 until children.length()) {
                    DisplayBlock(children.getJSONObject(index), onSaveProgress)
                }
            }
        }
    }
}
