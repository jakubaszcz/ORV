package com.onticentity.orv_reader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONObject

@Composable
fun DisplayBlock(block: JSONObject) {
    when (block.getString("type")) {
        "paragraph", "heading" -> {
            Box(modifier = Modifier.padding(4.dp)) {
                Text(block.getString("text"))
            }
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
