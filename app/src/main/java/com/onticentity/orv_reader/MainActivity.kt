package com.onticentity.orv_reader

import android.os.Bundle
import android.os.Build
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.onticentity.orv_reader.ui.components.SplashArtworkView
import com.onticentity.orv_reader.ui.theme.ORVReaderTheme
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onDestroy() {
        splashAnimator?.cancel()
        super.onDestroy()
    }

    private var splashAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        splash.setOnExitAnimationListener { provider ->
            // Respect the device's reduced-animation preference.
            if (Build.VERSION.SDK_INT >= 26 && !ValueAnimator.areAnimatorsEnabled()) {
                provider.remove()
            } else {
                val container = provider.view as? ViewGroup
                val artwork = SplashArtworkView(this)
                if (container != null) {
                    container.addView(artwork, ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                }
                splashAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 900L
                    interpolator = LinearInterpolator()
                    addUpdateListener { animation ->
                        val fraction = animation.animatedValue as Float
                        artwork.progress = fraction
                        provider.view.alpha = (1f - (fraction - 0.65f).coerceAtLeast(0f) / 0.35f).coerceIn(0f, 1f)
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            provider.remove()
                            splashAnimator = null
                        }
                    })
                    start()
                }
            }
        }

        val controller = WindowCompat.getInsetsController(window, window.decorView)

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        controller.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            ReaderRoot()
        }
    }
}
