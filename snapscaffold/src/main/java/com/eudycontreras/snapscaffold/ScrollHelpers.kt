package com.eudycontreras.snapscaffold

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
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
internal fun ScrollState.mappedThreshold(height: Float): Float {
    val scroll = value.f
    if (height <= MIN_OFFSET) return MIN_OFFSET
    val ratio = height / maxValue
    val offset = scroll / maxValue
    return mapRangeBounded(offset, MIN_OFFSET, ratio, MIN_OFFSET, MAX_OFFSET)
}

@Stable
internal fun LazyListState.mappedThreshold(height: Float): Float {
    val scroll = firstVisibleItemScrollOffset.f
    if (height <= MIN_OFFSET) return MIN_OFFSET
    val ratio = height / layoutInfo.visibleItemsInfo[0].size.f
    val offset = scroll / layoutInfo.visibleItemsInfo[0].size.f
    return mapRangeBounded(offset, MIN_OFFSET, ratio, MIN_OFFSET, MAX_OFFSET)
}

@Stable
internal val ScrollState.scrollDirection: State<ScrollDirection>
    @Composable get() {
        val scrollDirection = remember { mutableStateOf(ScrollDirection.None) }
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

@Stable
internal val LazyListState.scrollDirection: State<ScrollDirection>
    @Composable get() {
        val scrollDirection = remember { mutableStateOf(ScrollDirection.None) }
        LaunchedEffect(Unit) {
            launch {
                var lastScrollPosition = firstVisibleItemScrollOffset.absoluteValue
                snapshotFlow { firstVisibleItemScrollOffset.absoluteValue }.collectLatest {
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

internal val ScrollToTopSpec: AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = 400f
)

internal class SnapAreaScrollState(
    initialSnapHeight: Float,
    isSnapEnabled: Boolean,
    val listState: LazyListState,
) {
    var isSnapEnabled: Boolean by mutableStateOf(isSnapEnabled)
        private set

    var snapHeight: Float by mutableFloatStateOf(initialSnapHeight)
        private set

    val scrollOffset: Float by derivedStateOf {
        val index = listState.firstVisibleItemIndex
        val offset = listState.firstVisibleItemScrollOffset
        val mapped = mapRangeBounded(offset.f, MIN_OFFSET, snapHeight)
        if (index > 0) MAX_OFFSET else mapped.coerceFraction()
    }

    fun setSnappingHeight(snapHeight: Float) {
        this.snapHeight = snapHeight
    }

    fun enableSnapping(isEnable: Boolean) {
        this.isSnapEnabled = isEnable
    }

    companion object {
        @Composable
        fun rememberSnapAreaScrollState(
            initialSnapHeight: Dp = 1.dp,
            isSnapEnabled: Boolean = true,
            listState: LazyListState
        ): SnapAreaScrollState {
            val height = with(LocalDensity.current) { initialSnapHeight.toPx() }
            return remember(initialSnapHeight, listState) {
                SnapAreaScrollState(height, isSnapEnabled, listState)
            }
        }

        @Composable
        fun rememberSnapAreaScrollState(
            initialSnapHeight: Float,
            isSnapEnabled: Boolean = true,
            listState: LazyListState
        ): SnapAreaScrollState {
            return remember(initialSnapHeight, listState) {
                SnapAreaScrollState(initialSnapHeight, isSnapEnabled, listState)
            }
        }
    }
}

internal fun Modifier.snapAreaScrollBehaviour(
    state: SnapAreaScrollState,
): Modifier {
    return this.composed {
        val allowSnapping = remember { mutableStateOf(value = false) }
        val scrollDirection by state.listState.scrollDirection
        val isDragged by state.listState.interactionSource.collectIsDraggedAsState()

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    val offset = state.scrollOffset
                    if (consumed.y.absoluteValue > MAX_VELOCITY && offset < MAX_OFFSET) {
                        allowSnapping.value = true
                    }
                    return super.onPostFling(consumed, available)
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    allowSnapping.value = available.y.absoluteValue < MAX_VELOCITY
                    return super.onPreFling(available)
                }
            }
        }

        val snapAreaStateIn by remember {
            derivedStateOf {
                val threshold = SNAP_THRESHOLD
                val offset = state.scrollOffset
                val allowSnap = allowSnapping.value && state.isSnapEnabled
                val offsetValue = if (!isDragged && allowSnap) {
                    if (offset >= threshold && offset < MAX_OFFSET) MAX_OFFSET else {
                        if (offset < threshold && offset > MIN_OFFSET) -MAX_OFFSET else MIN_OFFSET
                    }
                } else MIN_OFFSET
                when (offsetValue) {
                    MAX_OFFSET -> CollapsibleAreaValue.Collapsed
                    -MAX_OFFSET -> CollapsibleAreaValue.Expanded
                    else -> CollapsibleAreaValue.Neutral
                }
            }
        }
        val snapAreaStateOut by remember {
            derivedStateOf {
                val direction = scrollDirection
                val areaState = snapAreaStateIn
                when {
                    areaState == CollapsibleAreaValue.Collapsed && direction == ScrollDirection.Up -> CollapsibleAreaValue.Expanded
                    areaState == CollapsibleAreaValue.Expanded && direction == ScrollDirection.Down -> CollapsibleAreaValue.Collapsed
                    else -> snapAreaStateIn
                }
            }
        }

        LaunchedEffect(state.snapHeight) {
            snapshotFlow { snapAreaStateOut }.collectLatest {
                val index = state.listState.firstVisibleItemIndex
                if (index <= 0) {
                    val current = state.listState.firstVisibleItemScrollOffset.f
                    when (it) {
                        CollapsibleAreaValue.Expanded -> {
                            state.listState.animateScrollBy(-current, ScrollSnapSpec)
                        }
                        CollapsibleAreaValue.Collapsed -> {
                            state.listState.animateScrollBy((state.snapHeight - current.absoluteValue), ScrollSnapSpec)
                        }
                        CollapsibleAreaValue.Neutral -> Unit
                    }
                }
            }
        }
        Modifier.nestedScroll(nestedScrollConnection)
    }
}
