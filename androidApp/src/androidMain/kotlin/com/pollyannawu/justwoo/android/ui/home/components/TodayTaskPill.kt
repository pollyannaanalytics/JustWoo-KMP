package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.design.DesignTokens
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * One red pill row inside the "Today's To-Do" card: HH:mm + title.
 */
@Composable
fun TodayTaskPill(
    task: Task,
    modifier: Modifier = Modifier,
) {
    val time = task.dueTime.toLocalDateTime(TimeZone.currentSystemDefault()).time
    val timeLabel = "%02d:%02d".format(time.hour, time.minute)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(JustWooShapes.XXLarge)
            .background(JustWooColors.UrgencyRed)
            .padding(horizontal = JustWooSpacing.Default, vertical = JustWooSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = timeLabel,
            color = Color.White,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = DesignTokens.FontSize.BodyLarge.sp,
        )
        Spacer(Modifier.size(JustWooSpacing.Medium))
        Text(
            text = task.title,
            color = Color.White,
            fontWeight = JustWooFontWeight.SemiBold,
            fontSize = DesignTokens.FontSize.BodyLarge.sp,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF7)
@Composable
private fun TodayTaskPillPreview() {
    JustWooTheme {
        TodayTaskPill(task = PreviewSamples.task(title = "買電池", hour = 15, minute = 0))
    }
}
