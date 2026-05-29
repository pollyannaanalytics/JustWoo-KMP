package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.R
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.design.DesignTokens

private fun monthlyDrawable(count: Int): Int = when {
    count >= 60 -> R.drawable.ic_monthly_tier4
    count >= 41 -> R.drawable.ic_monthly_tier3
    count >= 20 -> R.drawable.ic_monthly_tier2
    else -> R.drawable.ic_monthly_tier1
}

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
        Image(
            painter = painterResource(monthlyDrawable(count)),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun MonthlyStatCardTier1Preview() {
    JustWooTheme {
        MonthlyStatCard(count = 10, modifier = Modifier.padding(JustWooSpacing.Large))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun MonthlyStatCardTier2Preview() {
    JustWooTheme {
        MonthlyStatCard(count = 30, modifier = Modifier.padding(JustWooSpacing.Large))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun MonthlyStatCardTier3Preview() {
    JustWooTheme {
        MonthlyStatCard(count = 50, modifier = Modifier.padding(JustWooSpacing.Large))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun MonthlyStatCardTier4Preview() {
    JustWooTheme {
        MonthlyStatCard(count = 60, modifier = Modifier.padding(JustWooSpacing.Large))
    }
}
