package com.pollyannawu.justwoo.android.ui.nav.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.design.DesignTokens
import com.pollyannawu.justwoo.ui.nav.tasks.TaskQuickStatusComponent

/**
 * Overlay 渲染，覆蓋在 stack 上方。背景半透明遮罩 + 中央紅色卡片。
 * TODO: 串 TaskQuickStatusViewModel 抓 task 詳情，目前用 taskId 當佔位。
 */
@Composable
fun TaskQuickStatusOverlay(component: TaskQuickStatusComponent) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(onClick = component::onBack),   // 點外面關閉
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = JustWooSpacing.XLarge)
                .clip(RoundedCornerShape(24.dp))
                .background(JustWooColors.UrgencyRed)
                .clickable(enabled = false) {}        // 卡片內不傳遞點擊
                .padding(JustWooSpacing.XLarge),
            verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Medium),
        ) {
            Text(
                text = "15:00", // TODO: 從 ViewModel 抓真實 dueTime
                color = Color.White,
                fontWeight = JustWooFontWeight.Bold,
                fontSize = DesignTokens.FontSize.TitleLarge.sp,
            )
            Spacer(Modifier.height(JustWooSpacing.Small))
            Text(
                text = "Task #${component.taskId}",  // TODO: 從 ViewModel 抓 title
                color = Color.White,
                fontWeight = JustWooFontWeight.Bold,
                fontSize = DesignTokens.FontSize.Title.sp,
            )
            Spacer(Modifier.height(JustWooSpacing.Small))
            Text("• Assigner: —", color = Color.White)
            Text("• Due Date: —", color = Color.White)
            Text("• Note: —", color = Color.White)

            Spacer(Modifier.height(JustWooSpacing.Medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.Default),
            ) {
                QuickActionButton(
                    text = "Back",
                    background = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White,
                    onClick = component::onBack,
                    modifier = Modifier.weight(1f),
                )
                QuickActionButton(
                    text = "Complete",
                    background = Color.White,
                    contentColor = JustWooColors.UrgencyRed,
                    onClick = component::onComplete,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    background: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = JustWooFontWeight.ExtraBold,
        )
    }
}
