package com.onticentity.orv_reader.ui.gestures

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun chapterGestures(
    listState: LazyListState,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onBack: () -> Unit
): Modifier {
    val scope = rememberCoroutineScope()
    val threshold = with(LocalDensity.current) { 80.dp.toPx() }

    val latestNext by rememberUpdatedState(onNext)
    val latestPrevious by rememberUpdatedState(onPrevious)
    val latestBack by rememberUpdatedState(onBack)

    var touching by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableStateOf(0f) }
    var changeJob by remember { mutableStateOf<Job?>(null) }

    fun cancelPull() {
        changeJob?.cancel()
        changeJob = null
        pullDistance = 0f
    }

    val connection = remember(
        listState,
        threshold,
        canGoNext,
        canGoPrevious
    ) {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (
                    source == NestedScrollSource.UserInput &&
                    pullDistance * available.y < 0f
                ) {
                    cancelPull()
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (!touching || source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }

                val next = available.y < 0f &&
                        !listState.canScrollForward &&
                        canGoNext

                val previous = available.y > 0f &&
                        !listState.canScrollBackward &&
                        canGoPrevious

                if (!next && !previous) {
                    return Offset.Zero
                }

                pullDistance += available.y

                if (abs(pullDistance) >= threshold && changeJob == null) {
                    val goNext = next

                    changeJob = scope.launch {
                        delay(100L.milliseconds)

                        val stillAtEdge = if (goNext) {
                            !listState.canScrollForward
                        } else {
                            !listState.canScrollBackward
                        }

                        if (touching && stillAtEdge) {
                            // Only one chapter change per gesture.
                            touching = false
                            pullDistance = 0f
                            changeJob = null

                            if (goNext) {
                                latestNext()
                            } else {
                                latestPrevious()
                            }
                        }
                    }
                }

                // Consume only the movement beyond the edge.
                return Offset(0f, available.y)
            }
        }
    }

    return Modifier
        .nestedScroll(connection)
        .pointerInput(Unit) {
            // Observe the finger without consuming vertical scrolling.
            awaitEachGesture {
                awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial
                )

                cancelPull()
                touching = true

                try {
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                    } while (event.changes.any { it.pressed })
                } finally {
                    touching = false
                    cancelPull()
                }
            }
        }
        .pointerInput(threshold) {
            var horizontalDistance = 0f

            detectHorizontalDragGestures(
                onDragStart = {
                    horizontalDistance = 0f
                    touching = false
                    cancelPull()
                },
                onHorizontalDrag = { _, distance ->
                    horizontalDistance += distance
                },
                onDragEnd = {
                    if (abs(horizontalDistance) >= threshold) {
                        latestBack()
                    }
                    horizontalDistance = 0f
                },
                onDragCancel = {
                    horizontalDistance = 0f
                }
            )
        }
}
