package com.eudycontreras.snapscaffold.examples

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.eudycontreras.snapscaffold.CollapsibleSnapContentScaffold
import com.eudycontreras.snapscaffold.SnapScrollAreaState
import com.eudycontreras.snapscaffold.SnapScrollAreaStateScope
import com.eudycontreras.snapscaffold.rememberSnapScrollAreaState
import com.eudycontreras.snapscaffold.snapScrollAreaBehaviour
import com.eudycontreras.snapscaffold.ui.theme.Pink
import com.eudycontreras.snapscaffold.ui.theme.Purple100
import com.eudycontreras.snapscaffold.ui.theme.Purple40
import com.eudycontreras.snapscaffold.ui.theme.PurpleDark
import com.eudycontreras.snapscaffold.ui.theme.SnapscaffoldTheme
import kotlinx.coroutines.launch

val tabs = listOf("One", "Two")

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ScaffoldScreenScrollTabsState() {
    val coroutineScope = rememberCoroutineScope()
    val snapAreaState = rememberSnapScrollAreaState()
    val pagerState = rememberPagerState { tabs.size }

    CollapsibleSnapContentScaffold(
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
                        modifier = Modifier
                            .height(50.dp),
                        selected = index == pagerState.currentPage,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
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
private fun SnapScrollAreaStateScope. TabbedNestedScrollContent(
    snapAreaState: SnapScrollAreaState,
    pagerState: PagerState,
    bottomBarPadding: PaddingValues
) {
    val scrollState1 = rememberScrollState()
    val scrollState2 = rememberScrollState()
    HorizontalPager(
        state = pagerState) {
        when (val page = it) {
            0 -> {
                val isSelected by remember {
                    derivedStateOf { pagerState.currentPage == page }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .snapScrollAreaBehaviour(snapAreaState, scrollState1, isSelected),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = {
                        CollapsibleHeaderPaddingItem()
                        repeat(40) {
                            Box(
                                modifier = Modifier
                                    .height(80.dp)
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                                    .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                                    .background(Purple100, RoundedCornerShape(16.dp))
                            )
                        }
                        Spacer(modifier = Modifier.padding(bottomBarPadding))
                    }
                )
            }
            1 -> {
                val isSelected by remember {
                    derivedStateOf { pagerState.currentPage == page }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .snapScrollAreaBehaviour(snapAreaState, scrollState2, isSelected),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = {
                        CollapsibleHeaderPaddingItem()
                        repeat(20) {
                            Box(
                                modifier = Modifier
                                    .height(80.dp)
                                    .padding(horizontal = 12.dp)
                                    .fillMaxWidth()
                                    .shadow(1.dp, shape = RoundedCornerShape(16.dp))
                                    .background(Purple100, RoundedCornerShape(16.dp))
                            )
                        }
                        Spacer(modifier = Modifier.padding(bottomBarPadding))
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
        ScaffoldScreenScrollTabsState()
    }
}
