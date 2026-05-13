package com.pollyannawu.justwoo.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskStatus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Task card in the feed. The left rail + badge color track the due-date urgency:
 * red/yellow/green — matching the three "Task Space" variants in Figma.
 */
@Composable
fun TaskCard(
    task: Task,
    onClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val urgency = task.urgency()
    val palette = urgency.palette()
    val dueDate = task.dueTime.toLocalDateTime(TimeZone.currentSystemDefault()).date

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(JustWooColors.CreamElevated)
            .border(1.dp, JustWooColors.Outline, RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left urgency rail
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(palette.accent)
        )
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge,
                color = if (task.taskStatus == TaskStatus.DONE) JustWooColors.TextSecondary else JustWooColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JustWooColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(palette.background)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = urgency.label(dueDate),
                color = palette.accent,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
            )
        }
    }
}
