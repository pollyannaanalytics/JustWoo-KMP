package com.pollyannawu.justwoo.android.ui.task.components

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Drum-roll time picker showing hour (0–23) and minute (0–59) wheels side by side.
 * Purely visual — all state is hoisted via [onHourChange] / [onMinuteChange].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(JustWooShapes.Large)
            .border(1.dp, JustWooColors.Outline, JustWooShapes.Large)
            .padding(vertical = JustWooSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        WheelColumn(
            count = 24,
            initialIndex = hour,
            label = { "%02d".format(it) },
            onIndexChange = onHourChange,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = ":",
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = 28.sp,
        )
        WheelColumn(
            count = 60,
            initialIndex = minute,
            label = { "%02d".format(it) },
            onIndexChange = onMinuteChange,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    count: Int,
    initialIndex: Int,
    label: (Int) -> String,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemHeight = 44.dp
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialIndex - 1).coerceAtLeast(0),
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // With contentPadding(vertical = itemHeight) and total height = itemHeight*3,
    // firstVisibleItemIndex == the index of the item centred in the viewport after snap.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                onIndexChange(listState.firstVisibleItemIndex.coerceIn(0, count - 1))
            }
    }

    Box(modifier = modifier.height(itemHeight * 3)) {
        // Highlight band behind the selected (centre) item
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = JustWooSpacing.Medium)
                .height(itemHeight)
                .clip(JustWooShapes.Small)
                .then(
                    Modifier.border(
                        1.dp,
                        JustWooColors.Primary.copy(alpha = 0.3f),
                        JustWooShapes.Small,
                    )
                ),
        )
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(count) { index ->
                val isSelected = index == listState.firstVisibleItemIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .wrapContentHeight(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label(index),
                        color = if (isSelected) JustWooColors.Primary else JustWooColors.TextSecondary,
                        fontWeight = if (isSelected) JustWooFontWeight.Bold else JustWooFontWeight.Regular,
                        fontSize = if (isSelected) 22.sp else 18.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview(name = "Default — 09:30", showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun WheelTimePickerDefaultPreview() {
    var h by remember { mutableIntStateOf(9) }
    var m by remember { mutableIntStateOf(30) }
    JustWooTheme {
        WheelTimePicker(hour = h, minute = m, onHourChange = { h = it }, onMinuteChange = { m = it })
    }
}

@Preview(name = "Midnight — 00:00", showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun WheelTimePickerMidnightPreview() {
    JustWooTheme {
        WheelTimePicker(hour = 0, minute = 0, onHourChange = {}, onMinuteChange = {})
    }
}

@Preview(name = "Dark — 23:59", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun WheelTimePickerDarkPreview() {
    JustWooTheme {
        WheelTimePicker(hour = 23, minute = 59, onHourChange = {}, onMinuteChange = {})
    }
}
