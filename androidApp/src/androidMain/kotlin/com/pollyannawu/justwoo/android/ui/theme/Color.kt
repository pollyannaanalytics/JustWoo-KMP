package com.pollyannawu.justwoo.android.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * JustWoo palette inspired by the House-Cowork Figma file.
 *
 * Background: warm cream (#F5F1E7)
 * Primary action: warm yellow (#FBC04C)
 * Urgency colors for task spaces:
 *   - Red    (today)
 *   - Yellow (tomorrow)
 *   - Green  (day-after-tomorrow or later)
 */
object JustWooColors {
    val Cream = Color(0xFFF5F1E7)
    val CreamSurface = Color(0xFFFBF8EF)
    val CreamElevated = Color(0xFFFFFDF7)

    val Primary = Color(0xFFFBC04C)          // "Sign in" button / title
    val PrimaryDeep = Color(0xFFF2A614)
    val OnPrimary = Color(0xFFFFFFFF)

    val TextPrimary = Color(0xFF1F1F1F)
    val TextSecondary = Color(0xFF707070)
    val TextPlaceholder = Color(0xFFB1B1B1)

    val Outline = Color(0xFFE3DFD3)
    val OutlineFocused = Color(0xFF1F1F1F)

    val Error = Color(0xFFE0523B)
    val ErrorSoft = Color(0xFFF7E8E4)

    // Task urgency palette (from Task Space variants)
    val UrgencyRed = Color(0xFFE0523B)
    val UrgencyRedBg = Color(0xFFFBE2DC)
    val UrgencyYellow = Color(0xFFFBC04C)
    val UrgencyYellowBg = Color(0xFFFDEBC6)
    val UrgencyGreen = Color(0xFF6AAE6C)
    val UrgencyGreenBg = Color(0xFFDFEFDC)
}
