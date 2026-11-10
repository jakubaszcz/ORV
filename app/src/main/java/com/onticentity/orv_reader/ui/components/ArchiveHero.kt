package com.onticentity.orv_reader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Original vector scene: a ruined skyline below the constellations. */
@Composable
fun ArchiveHero(chapters: Int, episodes: Int) {
    Column {
        Row(Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("◈  STAR STREAM", color = MaterialTheme.colorScheme.primary,
                fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 2.sp)
            Text("READER ARCHIVE", color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        }
        Box(Modifier.fillMaxWidth().height(190.dp).background(Color(0xFF0B1626))) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawRect(Brush.radialGradient(listOf(Color(0xFF19445C), Color(0xFF080F1C)), center = Offset(w * .68f, h * .28f), radius = w * .8f))
                val stars = listOf(.14f to .24f, .30f to .13f, .46f to .30f, .64f to .16f, .79f to .26f, .87f to .10f)
                stars.zipWithNext().forEach { (a, b) ->
                    drawLine(Color(0x5573CADA), Offset(w*a.first,h*a.second), Offset(w*b.first,h*b.second), 1.dp.toPx())
                }
                stars.forEach { (x,y) ->
                    drawCircle(Color(0x1878E7EE), 7.dp.toPx(), Offset(w*x,h*y))
                    drawCircle(Color(0xFFB5F3F2), 1.5.dp.toPx(), Offset(w*x,h*y))
                }
                for (i in 0..19) {
                    val bh = h * (.13f + ((i * 7) % 9) * .022f)
                    val x = i * w / 19
                    drawRect(Color(0xFF091321), Offset(x, h*.84f-bh), Size(w/22, bh))
                    if (i % 3 == 0) drawLine(Color(0xFF223D4A), Offset(x+w/44,h*.84f-bh), Offset(x+w/44,h*.84f-bh-12.dp.toPx()), 1.dp.toPx())
                }
                // Rails converge on the last light of the city.
                for (x in listOf(.1f,.23f,.77f,.9f)) drawLine(Color(0xFF28505C), Offset(w*x,h), Offset(w*.53f,h*.72f), 1.dp.toPx())
                drawRect(Brush.verticalGradient(listOf(Color.Transparent,Color(0xFF080F1C))))
                val person = Path().apply {
                    moveTo(w*.55f,h*.65f); lineTo(w*.59f,h*.65f)
                    lineTo(w*.615f,h*.91f); lineTo(w*.535f,h*.91f); close()
                }
                drawPath(person,Color(0xFFD4E0DD))
                drawCircle(Color(0xFF040A11),h*.032f,Offset(w*.57f,h*.61f))
                drawLine(Color(0xFF040A11),Offset(w*.555f,h*.89f),Offset(w*.55f,h),3.dp.toPx())
                drawLine(Color(0xFF040A11),Offset(w*.59f,h*.89f),Offset(w*.60f,h),3.dp.toPx())
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("OMNISCIENT", fontFamily = FontFamily.Serif, fontSize = 34.sp,
            lineHeight = 38.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("READER’S VIEWPOINT", fontSize = 16.sp, letterSpacing = 3.sp,
            fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text("A story for the one reader who stayed.", fontFamily = FontFamily.Serif,
            fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Text("$chapters CHAPTERS", fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$episodes EPISODES", fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
