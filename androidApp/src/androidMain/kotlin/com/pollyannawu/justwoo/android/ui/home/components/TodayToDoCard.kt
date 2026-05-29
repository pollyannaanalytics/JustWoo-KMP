package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.home.TaskUrgency
import com.pollyannawu.justwoo.android.ui.home.urgency
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.design.DesignTokens
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

private data class TodoPage(
    val title: String,
    val tasks: List<Task>,
    val pillColor: Color,
    val textColor: Color,
    val emptyMessage: String,
)

@Composable
fun TodayToDoCard(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
) {
    val now = remember { Clock.System.now() }
    val zone = TimeZone.currentSystemDefault()

    val pages = remember(tasks) {
        listOf(
            TodoPage(
                title = "Overdue",
                tasks = tasks.filter { it.urgency(now, zone) == TaskUrgency.Past },
                pillColor = JustWooColors.UrgencyRed,
                textColor = Color.White,
                emptyMessage = "No overdue tasks — great job!",
            ),
            TodoPage(
                title = "Today's To-Do",
                tasks = tasks.filter { it.urgency(now, zone) == TaskUrgency.Red },
                pillColor = JustWooColors.UrgencyYellow,
                textColor = JustWooColors.TextPrimary,
                emptyMessage = "No tasks today — enjoy your day.",
            ),
            TodoPage(
                title = "Tomorrow",
                tasks = tasks.filter { it.urgency(now, zone) == TaskUrgency.Yellow },
                pillColor = JustWooColors.UrgencyBlue,
                textColor = Color.White,
                emptyMessage = "Nothing due tomorrow yet.",
            ),
        )
    }

    val pagerState = rememberPagerState(initialPage = 1) { pages.size }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(JustWooShapes.XLarge)
            .background(JustWooColors.CreamElevated)
            .padding(JustWooSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Small),
    ) {
        HorizontalPager(state = pagerState) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Small),
            ) {
                Text(
                    text = page.title,
                    color = JustWooColors.TextPrimary,
                    fontWeight = JustWooFontWeight.Bold,
                    fontSize = DesignTokens.FontSize.Title.sp,
                )
                if (page.tasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = JustWooSpacing.Medium),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = page.emptyMessage,
                            color = JustWooColors.TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    page.tasks.forEach { task ->
                        TodayTaskPill(
                            task = task,
                            pillColor = page.pillColor,
                            textColor = page.textColor,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = JustWooSpacing.XSmall),
            horizontalArrangement = Arrangement.Center,
        ) {
            pages.indices.forEach { i ->
                val selected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (selected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) JustWooColors.TextPrimary else JustWooColors.Outline
                        ),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun TodayToDoCardPreview() {
    JustWooTheme {
        TodayToDoCard(
            tasks = PreviewSamples.todayTasks,
            modifier = Modifier.padding(JustWooSpacing.Large),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun TodayToDoCardEmptyPreview() {
    JustWooTheme {
        TodayToDoCard(tasks = emptyList(), modifier = Modifier.padding(JustWooSpacing.Large))
    }
}
