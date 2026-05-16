package com.pollyannawu.justwoo.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.design.DesignTokens.FontSize

/**
 * Compose `Typography` built from shared [com.pollyannawu.justwoo.design.DesignTokens].
 * Semantic mapping onto Material slots stays on Android — iOS builds its own
 * SwiftUI `Font` hierarchy on top of the same tokens.
 *
 * Figma sticky-note styles mapped as:
 *   No.7 Hero  (40 Black)     → displayLarge
 *   No.8       (32 ExtraBold) → displayMedium
 *   No.2       (32 Bold)      → headlineLarge
 *   No.1       (24 SemiBold)  → headlineMedium
 *   No.3       (20 Bold)      → titleLarge
 *   No.4       (20 Regular)   → titleMedium
 *   No.6       (16 ExtraBold) → labelLarge
 *   No.5       (15 SemiBold)  → bodyLarge
 */
val JustWooTypography = Typography(
    displayLarge = textStyle(FontSize.Hero, JustWooFontWeight.Black),
    displayMedium = textStyle(FontSize.Display, JustWooFontWeight.ExtraBold),
    headlineLarge = textStyle(FontSize.Display, JustWooFontWeight.Bold),
    headlineMedium = textStyle(FontSize.Heading, JustWooFontWeight.SemiBold),
    titleLarge = textStyle(FontSize.TitleLarge, JustWooFontWeight.Bold),
    titleMedium = textStyle(FontSize.TitleLarge, JustWooFontWeight.Regular),
    labelLarge = textStyle(FontSize.Label, JustWooFontWeight.ExtraBold),
    bodyLarge = textStyle(FontSize.BodyLarge, JustWooFontWeight.SemiBold),
    bodyMedium = textStyle(FontSize.Body, JustWooFontWeight.Regular),
    bodySmall = textStyle(FontSize.Caption, JustWooFontWeight.Regular),
)

private fun textStyle(size: Float, weight: FontWeight): TextStyle =
    TextStyle(fontSize = size.sp, fontWeight = weight)
