package com.onticentity.orv_reader

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.onticentity.orv_reader.data.ThemeMode
import com.onticentity.orv_reader.data.observeTheme
import com.onticentity.orv_reader.data.saveTheme
import com.onticentity.orv_reader.ui.theme.ORVReaderTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun ReaderRoot() {
    val context = LocalContext.current
    val flow = remember(context) { observeTheme(context) }
    val mode by flow.collectAsState(initial = ThemeMode.DARK)

    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }

    ORVReaderTheme(mode = mode) {
        ReaderApp(
            themeMode = mode,
            onAppearance = {
                if (!saving) {
                    saving = true
                    val nextMode = mode.next()

                    scope.launch {
                        try {
                            saveTheme(context, nextMode)
                        } catch (error: CancellationException) {
                            throw error
                        }  finally {
                            saving = false
                        }
                    }
                }
            }
        )
    }
}