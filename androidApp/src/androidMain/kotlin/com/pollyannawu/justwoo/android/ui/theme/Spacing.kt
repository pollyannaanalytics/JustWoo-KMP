package com.pollyannawu.justwoo.android.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.design.DesignTokens

/**
 * Compose-friendly view of [DesignTokens.Spacing] — exposes the raw [Float]
 * values pre-wrapped as [Dp] so call sites read `padding(JustWooSpacing.Large)`
 * instead of `padding(20.dp)`.
 */
object JustWooSpacing {
    val XSmall: Dp = DesignTokens.Spacing.XSmall.dp
    val Small: Dp = DesignTokens.Spacing.Small.dp
    val Medium: Dp = DesignTokens.Spacing.Medium.dp
    val Default: Dp = DesignTokens.Spacing.Default.dp
    val Large: Dp = DesignTokens.Spacing.Large.dp
    val XLarge: Dp = DesignTokens.Spacing.XLarge.dp
    val XXLarge: Dp = DesignTokens.Spacing.XXLarge.dp
}
