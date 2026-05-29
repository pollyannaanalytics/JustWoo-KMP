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

@Composable
fun TodayTaskPill(
    task: Task,
    pillColor: Color = JustWooColors.UrgencyRed,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    val time = task.dueTime.toLocalDateTime(TimeZone.currentSystemDefault()).time
    val timeLabel = "%02d:%02d".format(time.hour, time.minute)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(JustWooShapes.XXLarge)
            .background(pillColor)
            .padding(horizontal = JustWooSpacing.Default, vertical = JustWooSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = timeLabel,
            color = textColor,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = DesignTokens.FontSize.BodyLarge.sp,
        )
        Spacer(Modifier.size(JustWooSpacing.Medium))
        Text(
            text = task.title,
            color = textColor,
            fontWeight = JustWooFontWeight.SemiBold,
            fontSize = DesignTokens.FontSize.BodyLarge.sp,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF7)
@Composable
private fun TodayTaskPillRedPreview() {
    JustWooTheme {
        TodayTaskPill(
            task = PreviewSamples.task(title = "買電池", hour = 15, minute = 0),
            pillColor = JustWooColors.UrgencyRed,
            textColor = Color.White,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF7)
@Composable
private fun TodayTaskPillYellowPreview() {
    JustWooTheme {
        TodayTaskPill(
            task = PreviewSamples.task(title = "帶豆漿去散步", hour = 18, minute = 0),
            pillColor = JustWooColors.UrgencyYellow,
            textColor = JustWooColors.TextPrimary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFDF7)
@Composable
private fun TodayTaskPillBluePreview() {
    JustWooTheme {
        TodayTaskPill(
            task = PreviewSamples.task(title = "倒垃圾", hour = 21, minute = 30),
            pillColor = JustWooColors.UrgencyBlue,
            textColor = Color.White,
        )
    }
}
