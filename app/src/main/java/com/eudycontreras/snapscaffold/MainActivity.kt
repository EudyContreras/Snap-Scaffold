package com.eudycontreras.snapscaffold

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.core.view.WindowCompat
import com.eudycontreras.snapscaffold.examples.ScaffoldScreenLazyScrollState
import com.eudycontreras.snapscaffold.examples.ScaffoldScreenScrollState
import com.eudycontreras.snapscaffold.examples.ScaffoldScreenScrollTabsState
import com.eudycontreras.snapscaffold.ui.theme.Purple80
import com.eudycontreras.snapscaffold.ui.theme.PurpleDark
import com.eudycontreras.snapscaffold.ui.theme.SnapscaffoldTheme
import kotlinx.coroutines.launch

internal enum class PageType {
    ScrollableTabs, Scrollable, LazyScrollable
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.White.toArgb(),
                Color.White.toArgb()
            )
        )

        super.onCreate(savedInstanceState)
        setContent {
            SnapscaffoldTheme {
                Surface(
                    modifier = Modifier
                        .background(PurpleDark)
                        .statusBarsPadding()
                        .fillMaxSize(),
                    color = Purple80
                ) {
                    val pages = listOf(PageType.ScrollableTabs, PageType.Scrollable, PageType.LazyScrollable)
                    val scope = rememberCoroutineScope()
                    val pagerState = rememberPagerState(pageCount = { pages.size} )
                    Column {
                        TabRow(
                            modifier = Modifier.height(50.dp),
                            contentColor = Color.White,
                            containerColor = PurpleDark,
                            selectedTabIndex = pagerState.currentPage,
                            divider = { }
                        ) {
                            pages.fastForEachIndexed { index, pageType ->
                                Tab(
                                    modifier = Modifier
                                        .height(50.dp),
                                    selected = index == pagerState.currentPage,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } }
                                ) {
                                    Text(text = pageType.name)
                                }
                            }
                        }
                        HorizontalPager(state = pagerState) {
                            val page = pages[it]
                            when (page) {
                                PageType.ScrollableTabs -> ScaffoldScreenScrollTabsState()
                                PageType.LazyScrollable -> ScaffoldScreenLazyScrollState()
                                PageType.Scrollable -> ScaffoldScreenScrollState()
                            }
                        }
                    }
                }
            }
        }
    }
}
