package com.pollyannawu.justwoo.android.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.R
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.design.DesignTokens

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
        Image(
            painter = painterResource(R.drawable.ic_task_space_banner),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(JustWooShapes.Large),
            contentScale = ContentScale.FillWidth,
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
