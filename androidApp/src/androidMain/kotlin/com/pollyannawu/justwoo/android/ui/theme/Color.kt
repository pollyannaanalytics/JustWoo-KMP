package com.pollyannawu.justwoo.android.ui.theme

import androidx.compose.ui.graphics.Color
import com.pollyannawu.justwoo.design.DesignTokens

/**
 * Compose-friendly wrapper around [DesignTokens.Colors]. Raw ARGB values live
 * in shared/commonMain so iOS can read the same palette via a Swift extension.
 */
object JustWooColors {
    val Cream = Color(DesignTokens.Colors.Cream)
    val CreamSurface = Color(DesignTokens.Colors.CreamSurface)
    val CreamElevated = Color(DesignTokens.Colors.CreamElevated)

    val Primary = Color(DesignTokens.Colors.Primary)
    val PrimaryDeep = Color(DesignTokens.Colors.PrimaryDeep)
    val OnPrimary = Color(DesignTokens.Colors.OnPrimary)

    val TextPrimary = Color(DesignTokens.Colors.TextPrimary)
    val TextSecondary = Color(DesignTokens.Colors.TextSecondary)
    val TextPlaceholder = Color(DesignTokens.Colors.TextPlaceholder)

    val Outline = Color(DesignTokens.Colors.Outline)
    val OutlineFocused = Color(DesignTokens.Colors.OutlineFocused)

    val Error = Color(DesignTokens.Colors.Error)
    val ErrorSoft = Color(DesignTokens.Colors.ErrorSoft)

    val UrgencyRed = Color(DesignTokens.Colors.UrgencyRed)
    val UrgencyRedBg = Color(DesignTokens.Colors.UrgencyRedBg)
    val UrgencyYellow = Color(DesignTokens.Colors.UrgencyYellow)
    val UrgencyYellowBg = Color(DesignTokens.Colors.UrgencyYellowBg)
    val UrgencyGreen = Color(DesignTokens.Colors.UrgencyGreen)
    val UrgencyGreenBg = Color(DesignTokens.Colors.UrgencyGreenBg)

    val UrgencyBlue = Color(DesignTokens.Colors.UrgencyBlue)
    val UrgencyBlueBg = Color(DesignTokens.Colors.UrgencyBlueBg)

    val AccentMint = Color(DesignTokens.Colors.AccentMint)
}
