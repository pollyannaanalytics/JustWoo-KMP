package com.pollyannawu.justwoo.android.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.design.DesignTokens

/**
 * Minimal text-based recreation of the "JUST WOO" wordmark that appears
 * at the top of the Sign in screen in the Figma file. The egg mascot
 * from the design can be swapped in later by dropping a drawable.
 */
@Composable
fun JustWooLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.XSmall, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "JUST",
            color = JustWooColors.TextPrimary,
            fontSize = DesignTokens.FontSize.Hero.sp,
            fontWeight = JustWooFontWeight.Black,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "WOO",
            color = JustWooColors.TextPrimary,
            fontSize = DesignTokens.FontSize.Hero.sp,
            fontWeight = JustWooFontWeight.Black,
            textAlign = TextAlign.Center,
        )
    }
}
