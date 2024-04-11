package com.eudycontreras.snapscaffold

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.eudycontreras.snapscaffold.ui.theme.SnapscaffoldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnapscaffoldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScaffoldScreen()
                }
            }
        }
    }
}

@Composable
fun ScaffoldScreen() {
    val snapAreaState = rememberSnapScrollAreaState()
    CollapsibleSnapContentScaffold(
        snapAreaState = snapAreaState,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        topBar = {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(Color.Yellow)
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
                    .fillMaxWidth()
                    .background(Color.Green)
            )
        },
        stickyHeader = {
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .background(Color.Blue)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(Color.Yellow.copy(0.4f))
            )
        },
        content = { bottomBarPadding ->
            repeat(20) {
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .background(Color.Red)
                )
            }
            Spacer(modifier = Modifier.padding(bottomBarPadding))
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ScaffoldScreenPreview() {
    SnapscaffoldTheme {
        ScaffoldScreen()
    }
}