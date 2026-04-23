package com.pollyannawu.justwoo.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColors = lightColorScheme(
    primary = JustWooColors.Primary,
    onPrimary = JustWooColors.OnPrimary,
    primaryContainer = JustWooColors.UrgencyYellowBg,
    onPrimaryContainer = JustWooColors.TextPrimary,
    secondary = JustWooColors.UrgencyGreen,
    onSecondary = JustWooColors.OnPrimary,
    background = JustWooColors.Cream,
    onBackground = JustWooColors.TextPrimary,
    surface = JustWooColors.CreamSurface,
    onSurface = JustWooColors.TextPrimary,
    surfaceVariant = JustWooColors.CreamElevated,
    onSurfaceVariant = JustWooColors.TextSecondary,
    outline = JustWooColors.Outline,
    error = JustWooColors.Error,
    onError = JustWooColors.OnPrimary,
    errorContainer = JustWooColors.ErrorSoft,
    onErrorContainer = JustWooColors.Error,
)

/**
 * Extra palette tokens not covered by Material3 ColorScheme (urgency colors).
 */
data class JustWooExtendedColors(
    val urgencyRed: androidx.compose.ui.graphics.Color,
    val urgencyRedBg: androidx.compose.ui.graphics.Color,
    val urgencyYellow: androidx.compose.ui.graphics.Color,
    val urgencyYellowBg: androidx.compose.ui.graphics.Color,
    val urgencyGreen: androidx.compose.ui.graphics.Color,
    val urgencyGreenBg: androidx.compose.ui.graphics.Color,
)

val LocalJustWooExtendedColors = staticCompositionLocalOf {
    JustWooExtendedColors(
        urgencyRed = JustWooColors.UrgencyRed,
        urgencyRedBg = JustWooColors.UrgencyRedBg,
        urgencyYellow = JustWooColors.UrgencyYellow,
        urgencyYellowBg = JustWooColors.UrgencyYellowBg,
        urgencyGreen = JustWooColors.UrgencyGreen,
        urgencyGreenBg = JustWooColors.UrgencyGreenBg,
    )
}

object JustWooTheme {
    val extended: JustWooExtendedColors
        @Composable get() = LocalJustWooExtendedColors.current
}

@Composable
fun JustWooTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalJustWooExtendedColors provides LocalJustWooExtendedColors.current) {
        MaterialTheme(
            colorScheme = LightColors,
            typography = JustWooTypography,
            content = content,
        )
    }
}
