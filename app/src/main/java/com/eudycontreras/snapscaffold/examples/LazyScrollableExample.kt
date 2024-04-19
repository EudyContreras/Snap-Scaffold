package com.eudycontreras.snapscaffold.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.eudycontreras.snapscaffold.CollapsibleSnapContentLazyScaffold
import com.eudycontreras.snapscaffold.rememberSnapLazyScrollAreaState
import com.eudycontreras.snapscaffold.snapLazyScrollAreaBehaviour
import com.eudycontreras.snapscaffold.ui.theme.Pink
import com.eudycontreras.snapscaffold.ui.theme.Purple100
import com.eudycontreras.snapscaffold.ui.theme.Purple40
import com.eudycontreras.snapscaffold.ui.theme.SnapscaffoldTheme

@Composable
internal fun ScaffoldScreenLazyScrollState() {
    val snapAreaState = rememberSnapLazyScrollAreaState()

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
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .fillMaxWidth()
                    .background(Purple40.copy(0.6f), RoundedCornerShape(16.dp))
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(Purple40)
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.snapLazyScrollAreaBehaviour(snapAreaState = snapAreaState),
            state = snapAreaState.listState,
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
                item { Spacer(modifier = Modifier.height(it.calculateBottomPadding())) }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScaffoldLazyScreenPreview() {
    SnapscaffoldTheme {
        ScaffoldScreenLazyScrollState()
    }
}