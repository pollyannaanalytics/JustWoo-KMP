package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * Yellow CTA card that navigates to the swipe-deck "Task Exploration".
 */
@Composable
fun TaskSpaceCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(JustWooShapes.XLarge)
            .background(JustWooColors.UrgencyYellowBg)
            .clickable { onClick() }
            .padding(horizontal = JustWooSpacing.XLarge, vertical = JustWooSpacing.XLarge),
        verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Small),
    ) {
        Text(
            text = "Task Space",
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Black,
            fontSize = DesignTokens.FontSize.Display.sp,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(JustWooShapes.Pill)
                .background(JustWooColors.AccentMint)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun TaskSpaceCardPreview() {
    JustWooTheme {
        TaskSpaceCard(onClick = {}, modifier = Modifier.padding(JustWooSpacing.Large))
    }
}
