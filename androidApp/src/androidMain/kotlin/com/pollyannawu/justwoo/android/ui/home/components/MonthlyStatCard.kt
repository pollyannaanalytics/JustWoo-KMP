package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.design.DesignTokens

/**
 * Yellow tile showing the monthly task count plus the egg/avatar slot.
 */
@Composable
fun MonthlyStatCard(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(JustWooShapes.XLarge)
            .background(JustWooColors.UrgencyYellowBg)
            .padding(horizontal = JustWooSpacing.XLarge, vertical = JustWooSpacing.XLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = count.toString(),
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Black,
            fontSize = DesignTokens.FontSize.HeroLarge.sp,
        )
        Spacer(Modifier.size(JustWooSpacing.Medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "tasks",
                color = JustWooColors.TextPrimary,
                fontWeight = JustWooFontWeight.SemiBold,
                fontSize = DesignTokens.FontSize.Label.sp,
            )
            Text(
                text = "/ month",
                color = JustWooColors.TextSecondary,
                fontSize = DesignTokens.FontSize.Body.sp,
            )
        }
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(JustWooColors.AccentMint)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun MonthlyStatCardPreview() {
    JustWooTheme {
        MonthlyStatCard(count = 10, modifier = Modifier.padding(JustWooSpacing.Large))
    }
}
