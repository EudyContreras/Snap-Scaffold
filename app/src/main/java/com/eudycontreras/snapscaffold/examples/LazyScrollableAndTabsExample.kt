package com.eudycontreras.snapscaffold.examples

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.lerp
import com.eudycontreras.snapscaffold.CollapsibleSnapContentLazyScaffold
import com.eudycontreras.snapscaffold.SnapLazyScrollAreaScope
import com.eudycontreras.snapscaffold.SnapLazyScrollAreaState
import com.eudycontreras.snapscaffold.rememberSnapLazyScrollAreaState
import com.eudycontreras.snapscaffold.snapLazyScrollAreaBehaviour
import com.eudycontreras.snapscaffold.ui.theme.Pink
import com.eudycontreras.snapscaffold.ui.theme.Purple100
import com.eudycontreras.snapscaffold.ui.theme.Purple40
import com.eudycontreras.snapscaffold.ui.theme.PurpleDark
import com.eudycontreras.snapscaffold.ui.theme.SnapscaffoldTheme
import kotlinx.coroutines.launch

val tabs = listOf("One", "Two")

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ScaffoldScreenScrollTabs() {
    val coroutineScope = rememberCoroutineScope()
    val snapAreaState = rememberSnapLazyScrollAreaState(rememberLazyListState())
    val pagerState = rememberPagerState { tabs.size }

    CollapsibleSnapContentLazyScaffold(
        snapAreaState = snapAreaState,
        topBar = {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(Purple40)
            )
        },
        collapsibleArea = {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = 1f - snapAreaState.scrollOffset
                        translationY = lerp(0f, -size.height, snapAreaState.scrollOffset)
                    }
                    .height(200.dp)
                    .padding(12.dp)
                    .fillMaxWidth()
                    .background(Pink, RoundedCornerShape(16.dp))
            )
        },
        stickyHeader = {
            val positions = remember { mutableMapOf(0 to Pair(0, 0), 1 to Pair(0, 0)) }
            LaunchedEffect(pagerState.currentPage) {
                val position = positions.getValue(pagerState.currentPage)
                if (positions.values.any { it.first >= 1 }) {
                    snapAreaState.scrollable.scrollToItem(maxOf(position.first, 1), position.second)
                } else {
                    snapAreaState.scrollable.scrollToItem(position.first, position.second)
                }
            }
            TabRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .fillMaxWidth()
                    .background(Purple40.copy(0.6f), RoundedCornerShape(16.dp))
                ,
                contentColor = Color.White,
                containerColor = PurpleDark,
                selectedTabIndex = pagerState.currentPage,
                divider = { }
            ) {
                tabs.fastForEachIndexed { index, tab ->
                    Tab(
                        modifier = Modifier.height(50.dp),
                        selected = index == pagerState.currentPage,
                        onClick = { coroutineScope.launch {
                            positions[pagerState.currentPage] =
                                snapAreaState.scrollable.firstVisibleItemIndex to snapAreaState.scrollable.firstVisibleItemScrollOffset
                            pagerState.animateScrollToPage(index)
                        } }
                    ) {
                        Text(text = tab)
                    }
                }
            }
        },
        content = { bottomBarPadding ->
            TabbedNestedScrollContent(snapAreaState, pagerState, bottomBarPadding)
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(Purple40)
            )
        },
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SnapLazyScrollAreaScope<LazyListScope>.TabbedNestedScrollContent(
    snapAreaState: SnapLazyScrollAreaState<LazyListState>,
    pagerState: PagerState,
    bottomBarPadding: PaddingValues
) {
    HorizontalPager(state = pagerState) {
        when (it) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.snapLazyScrollAreaBehaviour(snapAreaState = snapAreaState),
                    state = snapAreaState.scrollable,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = {
                        collapsibleHeaderPaddingItem(this)
                        items(20) {
                            Box(
                                modifier = Modifier
                                    .height(80.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                                    .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                                    .background(Purple100, RoundedCornerShape(16.dp))
                            )
                        }
                        item { Spacer(modifier = Modifier.padding(bottomBarPadding)) }
                    }
                )
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier.snapLazyScrollAreaBehaviour(snapAreaState = snapAreaState),
                    state = snapAreaState.scrollable,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = {
                        collapsibleHeaderPaddingItem(this)
                        items(80) {
                            Box(
                                modifier = Modifier
                                    .height(80.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                                    .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                                    .background(Purple100, RoundedCornerShape(16.dp))
                            )
                        }
                        item { Spacer(modifier = Modifier.padding(bottomBarPadding)) }
                    }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
private fun ScaffoldScreenPreview() {
    SnapscaffoldTheme {
        ScaffoldScreenScrollTabs()
    }
}
