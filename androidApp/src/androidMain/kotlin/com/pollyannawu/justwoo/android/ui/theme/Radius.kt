package com.pollyannawu.justwoo.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.design.DesignTokens

/**
 * Compose-friendly view of [DesignTokens.Radius]. [JustWooRadius] gives the
 * raw [Dp] (use when you only need one corner / a custom shape); [JustWooShapes]
 * pre-builds the common rounded shapes for `Modifier.clip()` / borders.
 */
object JustWooRadius {
    val Small: Dp = DesignTokens.Radius.Small.dp
    val Medium: Dp = DesignTokens.Radius.Medium.dp
    val Large: Dp = DesignTokens.Radius.Large.dp
    val XLarge: Dp = DesignTokens.Radius.XLarge.dp
    val XXLarge: Dp = DesignTokens.Radius.XXLarge.dp
    val Pill: Dp = DesignTokens.Radius.Pill.dp
}

object JustWooShapes {
    val Small = RoundedCornerShape(JustWooRadius.Small)
    val Medium = RoundedCornerShape(JustWooRadius.Medium)
    val Large = RoundedCornerShape(JustWooRadius.Large)
    val XLarge = RoundedCornerShape(JustWooRadius.XLarge)
    val XXLarge = RoundedCornerShape(JustWooRadius.XXLarge)
    val Pill = RoundedCornerShape(JustWooRadius.Pill)
}
