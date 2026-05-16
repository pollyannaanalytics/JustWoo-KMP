package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.design.DesignTokens

/**
 * Cream card containing today's task pills (or an empty-state message).
 */
@Composable
fun TodayToDoCard(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(JustWooShapes.XLarge)
            .background(JustWooColors.CreamElevated)
            .padding(JustWooSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Small),
    ) {
        Text(
            text = "Today's To-Do",
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = DesignTokens.FontSize.Title.sp,
        )
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = JustWooSpacing.Medium),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No tasks today — enjoy your day.",
                    color = JustWooColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            tasks.forEach { task ->
                TodayTaskPill(task = task)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun TodayToDoCardPreview() {
    JustWooTheme {
        TodayToDoCard(tasks = PreviewSamples.todayTasks, modifier = Modifier.padding(JustWooSpacing.Large))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun TodayToDoCardEmptyPreview() {
    JustWooTheme {
        TodayToDoCard(tasks = emptyList(), modifier = Modifier.padding(JustWooSpacing.Large))
    }
}
