package com.onticentity.orv_reader.ui.components

import android.content.Context
import android.graphics.*
import android.view.View
import com.onticentity.orv_reader.R
import androidx.compose.ui.graphics.toArgb
import com.onticentity.orv_reader.ui.theme.ReaderBackground
import kotlin.math.sin

/** A short, static-layout launch animation, matching the reader's charcoal palette. */
class SplashArtworkView(context: Context) : View(context) {
    private val logo = BitmapFactory.decodeResource(resources, R.drawable.orv_splash_transparent)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val dp = resources.displayMetrics.density
    var progress: Float = 0f
        set(value) { field = value; invalidate() }

    init { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        canvas.drawColor(ReaderBackground.toArgb())
        val glow = sin(progress * Math.PI).toFloat().coerceAtLeast(0f)
        paint.shader = RadialGradient(cx, cy, 230f * dp,
            intArrayOf(Color.argb((65 + 35 * glow).toInt(), 75, 80, 76), Color.TRANSPARENT),
            null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // A few quiet lights rather than a busy loading indicator.
        val stars = arrayOf(-100f to -100f, 94f to -58f, -83f to 80f, 105f to 104f)
        stars.forEachIndexed { i, (x, y) ->
            paint.color = Color.argb((35 + 65 * glow).toInt(), 187, 184, 174)
            val px = cx + x * dp
            val py = cy + (y - progress * (3 + i)) * dp
            canvas.drawCircle(px, py, (if (i % 2 == 0) 1f else .6f) * dp, paint)
        }
        val side = (176f + 8f * progress) * dp
        paint.color = Color.WHITE
        paint.alpha = 255
        // Screen blending hides the source logo's black background without modifying the asset.
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        canvas.drawBitmap(logo, null, RectF(cx-side/2, cy-side/2, cx+side/2, cy+side/2), paint)
        paint.xfermode = null
    }
}
