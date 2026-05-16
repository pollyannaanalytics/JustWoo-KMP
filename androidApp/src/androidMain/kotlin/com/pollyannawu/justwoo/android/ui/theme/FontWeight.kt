package com.pollyannawu.justwoo.android.ui.theme

import androidx.compose.ui.text.font.FontWeight
import com.pollyannawu.justwoo.design.DesignTokens

/**
 * Compose `FontWeight` values driven by [DesignTokens.FontWeight]. Use these
 * in place of Compose's built-in `FontWeight.Bold` etc. so the numeric scale
 * stays under the design system's control.
 */
object JustWooFontWeight {
    val Regular = FontWeight(DesignTokens.FontWeight.Regular)
    val Medium = FontWeight(DesignTokens.FontWeight.Medium)
    val SemiBold = FontWeight(DesignTokens.FontWeight.SemiBold)
    val Bold = FontWeight(DesignTokens.FontWeight.Bold)
    val ExtraBold = FontWeight(DesignTokens.FontWeight.ExtraBold)
    val Black = FontWeight(DesignTokens.FontWeight.Black)
}
