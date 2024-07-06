package com.eudycontreras.snapscaffold


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
internal fun CollapsingLayout(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    topBar: @Composable ColumnScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    var collapsingTopHeight by remember { mutableFloatStateOf(MIN_OFFSET) }

    var offset by remember { mutableFloatStateOf(MIN_OFFSET) }

    fun calculateOffset(delta: Float): Offset {
        val oldOffset = offset
        val newOffset = (oldOffset + delta).coerceIn(-collapsingTopHeight, MIN_OFFSET)
        offset = newOffset
        return Offset(MIN_OFFSET, newOffset - oldOffset)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val draggingUp = available.y < 0
                return when {
                    draggingUp && (offset == -collapsingTopHeight) -> Offset.Zero
                    else -> calculateOffset(available.y)
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = when {
                available.y <= MIN_OFFSET -> Offset.Zero
                offset == MIN_OFFSET -> Offset.Zero
                else -> calculateOffset(available.y)
            }
        }
    }

    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .nestedScroll(nestedScrollConnection)
        ,
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor)
                .offset { IntOffset(x = 0, y = (collapsingTopHeight + offset).roundToInt()) },
            content = content,
        )
        Box(
            modifier = Modifier
                .onSizeChanged { size -> collapsingTopHeight = size.height.toFloat() }
                .offset { IntOffset(x = 0, y = offset.roundToInt()) },
            content = {
                Column(modifier = Modifier
                    .background(backgroundColor)
                    .graphicsLayer {
                        val height = collapsingTopHeight
                        val ratio = MAX_OFFSET - mapRangeBounded(offset.absoluteValue, MIN_OFFSET, height)
                        alpha = mapRangeBounded(ratio, 0.3f, MAX_OFFSET)
                    }, content = topBar)
            }
        )
    }
}