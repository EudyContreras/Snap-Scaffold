package com.eudycontreras.snapscaffold

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.composed
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * State holder for managing the collapsible snap behavior of a scrollable area.
 *
 * @param snapAreaHeight The height of the collapsible snap area.
 * @param isSnapEnabled Flag to enable or disable snapping behavior.
 * @param listState The [LazyListState] object to be used for tracking scroll position.
 */
class SnapLazyScrollAreaState(
    snapAreaHeight: Float,
    isSnapEnabled: Boolean,
    val listState: LazyListState,
) {
    var isSnapEnabled: Boolean by mutableStateOf(isSnapEnabled)
        private set

    var snapAreaHeight: Float by mutableFloatStateOf(snapAreaHeight)
        private set

    /**
     * Returns the current scroll offset within the snap area.
     */
    val scrollOffset: Float by derivedStateOf {
        val index = listState.firstVisibleItemIndex
        val offset = listState.firstVisibleItemScrollOffset
        val mapped = mapRangeBounded(offset.f, MIN_OFFSET, snapAreaHeight)
        if (index > 0) MAX_OFFSET else mapped.coerceFraction()
    }

    /**
     * Sets the height of the collapsible snap area.
     *
     * @param snapHeight The new height of the snap area.
     */
    fun setCollapsibleSnapAreaHeight(snapHeight: Float) {
        this.snapAreaHeight = snapHeight
    }

    /**
     * Enables or disables snapping behavior.
     *
     * @param isEnable Flag to enable or disable snapping.
     */
    fun enableSnapping(isEnable: Boolean) {
        this.isSnapEnabled = isEnable
    }
}

/**
 * Creates and remembers a [SnapScrollAreaState].
 *
 * @param listState The [LazyListState] object to be used for tracking scroll position.
 * @param initialSnapHeight The initial height of the snap area.
 * @param isSnapEnabled Flag to enable or disable snapping behavior.
 * @return [SnapScrollAreaState] object to manage snap behavior.
 */
@Composable
fun rememberSnapLazyScrollAreaState(
    listState: LazyListState = rememberLazyListState(),
    initialSnapHeight: Dp = 1.dp,
    isSnapEnabled: Boolean = true
): SnapLazyScrollAreaState {
    val height = with(LocalDensity.current) { initialSnapHeight.toPx() }
    return remember(initialSnapHeight, listState) {
        SnapLazyScrollAreaState(height, isSnapEnabled, listState)
    }
}

/**
 * Receiver scope which is used by CollapsibleScaffold.
 */
interface SnapLazyScrollAreaScope {
    val snapHeight: Float
    val scrollOffset: Float
    fun collapsibleHeaderPaddingItem(scope: LazyListScope)
}

private class SnapLazyScrollAreaScopeImpl(
    private val snapAreaScrollState: SnapLazyScrollAreaState
) : SnapLazyScrollAreaScope {
    override val snapHeight: Float
        get() = snapAreaScrollState.snapAreaHeight

    override val scrollOffset: Float
        get() = snapAreaScrollState.scrollOffset

    override fun collapsibleHeaderPaddingItem(scope: LazyListScope) = with(scope) {
        item {
            Spacer(
                modifier = Modifier
                    .height(height = with(LocalDensity.current) { snapHeight.toDp() })
            )
        }
    }
}

/**
 * Composable function for a collapsible snap content scaffold.
 *
 * This scaffold enables the creation of a flexible and customizable
 * scaffold layout with collapsible snap behavior, allowing for a smooth
 * and intuitive user experience when scrolling through content.
 * It leverages Jetpack Compose's compositional model to encapsulate and manage the complex
 * behavior of the scaffold.
 *
 * @param modifier The modifier for the scaffold.
 * @param snapAreaState The state holder for managing the collapsible snap behavior.
 * @param topBar The composable function for the top bar of the scaffold.
 * @param bottomBar The composable function for the bottom bar of the scaffold.
 * @param stickyHeader The composable function for the sticky header of the scaffold.
 * @param collapsibleArea The composable function for the collapsible area of the scaffold.
 * @param content The composable function for the main content of the scaffold.
 */
@Composable
fun CollapsibleSnapContentLazyScaffold(
    modifier: Modifier = Modifier,
    snapAreaState: SnapLazyScrollAreaState,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    stickyHeader: @Composable () -> Unit = {},
    collapsibleArea: @Composable BoxScope.() -> Unit,
    content: @Composable SnapLazyScrollAreaScope.(PaddingValues) -> Unit,
) {
    val snapScope = remember(snapAreaState) { SnapLazyScrollAreaScopeImpl(snapAreaState) }

    CollapsibleSnapContentLazyScaffoldLayout(
        modifier = modifier.fillMaxSize(),
        state = snapAreaState,
        topBar = topBar,
        stickyHeader = stickyHeader,
        collapsibleArea = {
            Box(
                modifier = Modifier
                    .onGloballyPositioned {
                        val height = it.size.height.f
                        snapAreaState.setCollapsibleSnapAreaHeight(height)
                    },
                content = collapsibleArea
            )
        },
        bottomBar = bottomBar,
        content = { snapScope.content(it) }
    )
}

@Composable
@UiComposable
private fun CollapsibleSnapContentLazyScaffoldLayout(
    modifier: Modifier,
    state: SnapLazyScrollAreaState,
    topBar: @Composable @UiComposable () -> Unit,
    stickyHeader: @Composable @UiComposable () -> Unit,
    collapsibleArea: @Composable @UiComposable () -> Unit,
    bottomBar: @Composable @UiComposable () -> Unit,
    content: @Composable @UiComposable (PaddingValues) -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        layout(layoutWidth, layoutHeight) {
            val topBarPlaceables = subcompose(
                slotId = LazyScaffoldContent.TopBar,
                content = topBar
            ).fastMap { it.measure(looseConstraints) }

            val bottomBarPlaceables = subcompose(
                slotId = LazyScaffoldContent.BottomBar,
                content = bottomBar
            ).fastMap { it.measure(looseConstraints) }

            val collapseAreaPlaceables = subcompose(
                slotId = LazyScaffoldContent.CollapsibleContent,
                content = collapsibleArea
            ).fastMap { it.measure(looseConstraints) }

            val stickyHeaderPlaceables = subcompose(
                slotId = LazyScaffoldContent.StickyHeader,
                content = stickyHeader
            ).fastMap { it.measure(looseConstraints) }

            val topBarHeight = topBarPlaceables.fastMaxBy { it.height }?.height ?: 0
            val bottomBarHeight = bottomBarPlaceables.fastMaxBy { it.height }?.height ?: 0
            val collapseAreaHeight = collapseAreaPlaceables.fastMaxBy { it.height }?.height ?: 0
            val stickyHeaderHeight = stickyHeaderPlaceables.fastMaxBy { it.height }?.height ?: 0
            val contentMaxHeight = layoutHeight - (topBarHeight + stickyHeaderHeight)

            val bodyContentPlaceables = subcompose(slotId = LazyScaffoldContent.BodyContent) {
                val bottomBarPadding = bottomBarHeight.toDp()
                val innerPadding = PaddingValues(bottom = bottomBarPadding)
                content(innerPadding)
            }.fastMap { it.measure(looseConstraints.copy(maxHeight = contentMaxHeight)) }

            val offset = (MAX_OFFSET - state.scrollOffset.absoluteValue)

            collapseAreaPlaceables.fastForEach {
                it.place(0, topBarHeight)
            }
            bodyContentPlaceables.fastForEach {
                it.place(0, stickyHeaderHeight + topBarHeight)
            }
            stickyHeaderPlaceables.fastForEach {
                it.place(0, (collapseAreaHeight * offset).roundToInt() + topBarHeight)
            }
            bottomBarPlaceables.fastForEach {
                it.place(0, layoutHeight - bottomBarHeight)
            }
            topBarPlaceables.fastForEach {
                it.place(0, 0)
            }
        }
    }
}

private enum class LazyScaffoldContent {
    TopBar,
    BottomBar,
    BodyContent,
    CollapsibleContent,
    StickyHeader
}

fun Modifier.snapLazyScrollAreaBehaviour(
    state: SnapLazyScrollAreaState,
): Modifier {
    return this.composed {
        val allowSnapping = remember { mutableStateOf(value = false) }
        val scrollDirection by state.listState.scrollDirection
        val isDragged by state.listState.interactionSource.collectIsDraggedAsState()

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity {
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

        LaunchedEffect(state.snapAreaHeight) {
            snapshotFlow { snapAreaStateOut }.collectLatest {
                val index = state.listState.firstVisibleItemIndex
                if (index <= 0) {
                    val current = state.listState.firstVisibleItemScrollOffset.f
                    when (it) {
                        CollapsibleAreaValue.Expanded -> {
                            state.listState.animateScrollBy(-current, ScrollSnapSpec)
                        }

                        CollapsibleAreaValue.Collapsed -> {
                            state.listState.animateScrollBy(
                                (state.snapAreaHeight - current.absoluteValue),
                                ScrollSnapSpec
                            )
                        }

                        CollapsibleAreaValue.Neutral -> Unit
                    }
                }
            }
        }

        Modifier.nestedScroll(nestedScrollConnection)
    }
}