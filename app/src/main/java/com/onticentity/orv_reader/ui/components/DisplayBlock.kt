package com.onticentity.orv_reader.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
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
                    fontFamily = Charter,
                    fontSize = if (inWindow) 17.sp else 18.sp,
                    lineHeight = if (inWindow) 27.sp else 29.sp,
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
                style = if (inWindow) MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Charter,
                    fontSize = 17.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.sp
                ) else when (level) {
                    1 -> MaterialTheme.typography.headlineLarge
                    2 -> MaterialTheme.typography.headlineMedium
                    else -> MaterialTheme.typography.titleLarge
                },
                modifier = Modifier.padding(top = 4.dp, bottom = if (inWindow) 4.dp else 12.dp)
            )
        }

        "box" -> {
            val children = block.getJSONArray("blocks")

            val ruleColor = MaterialTheme.colorScheme.outlineVariant
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    .drawBehind {
                        drawLine(ruleColor, Offset(0f, 0f), Offset(0f, size.height), 1.dp.toPx())
                    }
                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (index in 0 until children.length()) {
                    DisplayBlock(children.getJSONObject(index), onSaveProgress, inWindow = true)
                }
            }
        }
    }
}
