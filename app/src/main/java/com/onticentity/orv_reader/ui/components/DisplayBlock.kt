package com.onticentity.orv_reader.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import com.onticentity.orv_reader.ui.theme.Charter

@Composable
fun DisplayBlock(block: JSONObject, onSaveProgress: (String) -> Unit, inWindow: Boolean = false) {
    when (block.getString("type")) {
        "paragraph" -> {
            Text(
                text = block.getString("text"),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = if (inWindow) MaterialTheme.typography.bodyLarge.fontFamily else Charter,
                    fontSize = 18.sp,
                    lineHeight = 29.sp,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onDoubleClick = { onSaveProgress(block.getString("id")) }
                    )
            )
        }

        "heading" -> {
            val level = block.optInt("level", 2)

            Text(
                text = block.getString("text"),
                style = when (level) {
                    1 -> MaterialTheme.typography.headlineLarge
                    2 -> MaterialTheme.typography.headlineMedium
                    else -> MaterialTheme.typography.titleLarge
                },
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )
        }

        "box" -> {
            val children = block.getJSONArray("blocks")

            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (index in 0 until children.length()) {
                        DisplayBlock(children.getJSONObject(index), onSaveProgress, inWindow = true)
                    }
                }
            }
        }
    }
}
