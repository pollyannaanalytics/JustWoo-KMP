package com.pollyannawu.justwoo.design

/**
 * Cross-platform design tokens for JustWoo.
 *
 * Values are kept as primitives so they can be consumed from both Compose
 * (Android) and SwiftUI (iOS) without depending on platform UI types:
 *
 *   - Colors: ARGB encoded as [Long] (matches Compose's `Color(Long)` and
 *     decomposes cleanly into channels on iOS).
 *   - Sizes / spacing / radii: [Float] in dp / sp / pt — platforms apply the
 *     unit on their side.
 *   - Font weights: [Int] using the CSS / Material numeric scale.
 *
 * Semantic styles (e.g. "what is a heading") stay on each platform — this
 * file only owns the raw values.
 */
object DesignTokens {

    object Colors {
        // Background surfaces (cream palette from Figma)
        const val Cream: Long = 0xFFF5F1E7
        const val CreamSurface: Long = 0xFFFBF8EF
        const val CreamElevated: Long = 0xFFFFFDF7

        // Primary (yellow CTA)
        const val Primary: Long = 0xFFFBC04C
        const val PrimaryDeep: Long = 0xFFF2A614
        const val OnPrimary: Long = 0xFFFFFFFF

        // Text
        const val TextPrimary: Long = 0xFF1F1F1F
        const val TextSecondary: Long = 0xFF707070
        const val TextPlaceholder: Long = 0xFFB1B1B1

        // Strokes
        const val Outline: Long = 0xFFE3DFD3
        const val OutlineFocused: Long = 0xFF1F1F1F

        // Error
        const val Error: Long = 0xFFE0523B
        const val ErrorSoft: Long = 0xFFF7E8E4

        // Task urgency
        const val UrgencyRed: Long = 0xFFE0523B
        const val UrgencyRedBg: Long = 0xFFFBE2DC
        const val UrgencyYellow: Long = 0xFFFBC04C
        const val UrgencyYellowBg: Long = 0xFFFDEBC6
        const val UrgencyGreen: Long = 0xFF6AAE6C
        const val UrgencyGreenBg: Long = 0xFFDFEFDC

        const val UrgencyBlue: Long = 0xFF4A90D9
        const val UrgencyBlueBg: Long = 0xFFDAEAF8

        // Accent (egg / Task Space dinosaur band)
        const val AccentMint: Long = 0xFFB3E5DA
    }

    /** Font sizes in sp (Android) / pt (iOS). */
    object FontSize {
        const val Caption: Float = 12f
        const val Body: Float = 14f
        const val BodyLarge: Float = 15f
        const val Label: Float = 16f
        const val Title: Float = 18f
        const val TitleLarge: Float = 20f
        const val Heading: Float = 24f
        const val Stat: Float = 28f
        const val Display: Float = 32f
        const val Hero: Float = 40f
        const val HeroLarge: Float = 48f
    }

    /** CSS / Material numeric scale. */
    object FontWeight {
        const val Regular: Int = 400
        const val Medium: Int = 500
        const val SemiBold: Int = 600
        const val Bold: Int = 700
        const val ExtraBold: Int = 800
        const val Black: Int = 900
    }

    /** Spacing in dp (Android) / pt (iOS). */
    object Spacing {
        const val XSmall: Float = 4f
        const val Small: Float = 8f
        const val Medium: Float = 12f
        const val Default: Float = 16f
        const val Large: Float = 20f
        const val XLarge: Float = 24f
        const val XXLarge: Float = 32f
    }

    /** Corner radii in dp (Android) / pt (iOS). */
    object Radius {
        const val Small: Float = 8f
        const val Medium: Float = 12f
        const val Large: Float = 16f
        const val XLarge: Float = 20f
        const val XXLarge: Float = 24f
        const val Pill: Float = 28f
    }
}
