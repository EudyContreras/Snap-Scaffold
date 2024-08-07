package com.eudycontreras.snapscaffold

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

internal const val MAX_VELOCITY = 2500
internal const val SNAP_THRESHOLD = 0.2f

internal enum class CollapsibleAreaValue {
    Collapsed, Expanded, Neutral
}

internal enum class ScrollDirection {
    Up, Down, None
}

@Stable
internal val ScrollableState.scrollDirection: State<ScrollDirection>
    @Composable get() {
        val scrollDirection = remember { mutableStateOf(ScrollDirection.None) }
        val value by remember {
            derivedStateOf {
                when (this) {
                    is ScrollState -> value
                    is LazyListState -> firstVisibleItemScrollOffset.absoluteValue
                    is LazyGridState -> firstVisibleItemScrollOffset.absoluteValue
                    is LazyStaggeredGridState -> firstVisibleItemScrollOffset.absoluteValue
                    else -> throw UnsupportedScrollableStateException()
                }
            }
        }
        LaunchedEffect(Unit) {
            launch {
                var lastScrollPosition = value
                snapshotFlow { value }.collectLatest {
                    scrollDirection.value = if (it > lastScrollPosition) {
                        ScrollDirection.Down
                    } else if (it < lastScrollPosition) {
                        ScrollDirection.Up
                    } else ScrollDirection.None
                    lastScrollPosition = it
                }
            }
        }
        return scrollDirection
    }

internal val ScrollSnapSpec: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 200f
)