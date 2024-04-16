package com.eudycontreras.snapscaffold

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
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
 * @param scrollState The [ScrollState] object to be used for tracking scroll position.
 */
@Stable
class SnapScrollAreaState(
    snapAreaHeight: Float,
    isSnapEnabled: Boolean,
    val scrollState: ScrollState,
) {
    var isSnapEnabled: Boolean by mutableStateOf(isSnapEnabled)
        private set

    var snapAreaHeight: Float by mutableFloatStateOf(snapAreaHeight)
        private set

    /**
     * Returns the current scroll offset within the snap area.
     */
    @Stable
    val scrollOffset: Float
        get() = scrollState.mappedThreshold(snapAreaHeight)

    @Stable
    private fun ScrollState.mappedThreshold(height: Float): Float {
        val scroll = value.f
        if (height <= MIN_OFFSET) return MIN_OFFSET
        val ratio = height / maxValue
        val offset = scroll / maxValue
        return mapRangeBounded(offset, MIN_OFFSET, ratio, MIN_OFFSET, MAX_OFFSET)
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
 * @param scrollState The [ScrollState] object to be used for tracking scroll position.
 * @param initialSnapHeight The initial height of the snap area.
 * @param isSnapEnabled Flag to enable or disable snapping behavior.
 * @return [SnapScrollAreaState] object to manage snap behavior.
 */
@Composable
fun rememberSnapScrollAreaState(
    scrollState: ScrollState = rememberScrollState(),
    initialSnapHeight: Dp = Dp.Hairline,
    isSnapEnabled: Boolean = true,
): SnapScrollAreaState {
    val height = with(LocalDensity.current) { initialSnapHeight.toPx() }
    return remember(initialSnapHeight, scrollState) {
        SnapScrollAreaState(height, isSnapEnabled, scrollState)
    }
}

/**
 * Receiver scope which is used by CollapsibleScaffold.
 */
interface SnapScrollAreaStateScope {
    val snapHeight: Float
    val scrollOffset: Float

    @Composable
    fun CollapsibleHeaderPaddingItem()
}

private class SnapScrollAreaStateScopeImpl(
    private val snapAreaScrollState: SnapScrollAreaState
) : SnapScrollAreaStateScope {
    override val snapHeight: Float
        get() = snapAreaScrollState.snapAreaHeight

    override val scrollOffset: Float
        get() = snapAreaScrollState.scrollOffset

    @Composable
    override fun CollapsibleHeaderPaddingItem() {
        Spacer(
            modifier = Modifier
                .height(height = with(LocalDensity.current) { snapHeight.toDp() })
        )
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
fun CollapsibleSnapContentScaffold(
    modifier: Modifier = Modifier,
    snapAreaState: SnapScrollAreaState,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    stickyHeader: @Composable () -> Unit = {},
    collapsibleArea: @Composable BoxScope.() -> Unit,
    content: @Composable SnapScrollAreaStateScope.(bottomBarPadding: PaddingValues) -> Unit,
) {
    val snapScope = remember(snapAreaState) { SnapScrollAreaStateScopeImpl(snapAreaState) }

    CollapsibleSnapContentScaffoldLayout(
        modifier = modifier,
        topBar = topBar,
        state = snapAreaState,
        bottomBar = bottomBar,
        collapsibleArea = {
            Box(
                modifier = Modifier
                    .clipToBounds()
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        snapAreaState.setCollapsibleSnapAreaHeight(it.size.height.toFloat())
                    },
                content = collapsibleArea
            )
        },
        stickyHeader = stickyHeader,
        content = { padding -> snapScope.content(padding)}
    )
}

@Composable
@UiComposable
private fun CollapsibleSnapContentScaffoldLayout(
    modifier: Modifier,
    state: SnapScrollAreaState,
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
                slotId = ScaffoldContent.TopBar,
                content = topBar
            ).fastMap { it.measure(looseConstraints) }

            val bottomBarPlaceables = subcompose(
                slotId = ScaffoldContent.BottomBar,
                content = bottomBar
            ).fastMap { it.measure(looseConstraints) }

            val collapseAreaPlaceables = subcompose(
                slotId = ScaffoldContent.CollapseContent,
                content = collapsibleArea
            ).fastMap { it.measure(looseConstraints) }

            val stickyHeaderPlaceables = subcompose(
                slotId = ScaffoldContent.StickyHeader,
                content = stickyHeader
            ).fastMap { it.measure(looseConstraints) }

            val topBarHeight = topBarPlaceables.fastMaxBy { it.height }?.height ?: 0
            val bottomBarHeight = bottomBarPlaceables.fastMaxBy { it.height }?.height ?: 0
            val collapseAreaHeight = collapseAreaPlaceables.fastMaxBy { it.height }?.height ?: 0
            val stickyHeaderHeight = stickyHeaderPlaceables.fastMaxBy { it.height }?.height ?: 0
            val contentMaxHeight = layoutHeight - (topBarHeight + stickyHeaderHeight)

            val bodyContentPlaceables = subcompose(slotId = ScaffoldContent.BodyContent) {
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

private enum class ScaffoldContent {
    TopBar,
    BottomBar,
    BodyContent,
    CollapseContent,
    StickyHeader
}

fun Modifier.snapScrollAreaBehaviour(
    snapAreaState: SnapScrollAreaState
): Modifier {
    return this.composed {
        val snapHeight = snapAreaState.snapAreaHeight
        val scrollState = snapAreaState.scrollState
        val allowSnapping = remember { mutableStateOf(value = false) }
        val scrollDirection by scrollState.scrollDirection
        val isDragged by scrollState.interactionSource.collectIsDraggedAsState()

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity {
                    val offset = snapAreaState.scrollOffset
                    if (consumed.y.absoluteValue > MAX_VELOCITY && offset < 1F) {
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
                val offset = snapAreaState.scrollOffset
                val allowSnap = allowSnapping.value && snapAreaState.isSnapEnabled
                val offsetValue = if (!isDragged && allowSnap) {
                    when {
                        offset >= threshold && offset < MAX_OFFSET -> MAX_OFFSET
                        offset < threshold && offset > MIN_OFFSET -> -MAX_OFFSET
                        else -> MAX_OFFSET
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

        LaunchedEffect(snapHeight) {
            snapshotFlow { snapAreaStateOut }.collectLatest {
                when (it) {
                    CollapsibleAreaValue.Expanded -> scrollState.animateScrollTo(0, ScrollSnapSpec)
                    CollapsibleAreaValue.Collapsed -> scrollState.animateScrollTo(snapHeight.roundToInt(), ScrollSnapSpec)
                    CollapsibleAreaValue.Neutral -> {}
                }
            }
        }

        Modifier
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(scrollState)
    }
}