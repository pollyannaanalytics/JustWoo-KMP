package com.pollyannawu.justwoo.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type scale transcribed from the Figma file's numbered styles:
 *   No.1: 24 SemiBold
 *   No.2: 32 Bold
 *   No.3: 20 Bold
 *   No.4: 20 Regular
 *   No.5: 15 SemiBold
 *   No.6: 16 ExtraBold
 *   No.7: 40 Black
 *   No.8: 32 ExtraBold
 */
val JustWooTypography = Typography(
    // No.7 - 40 Black: hero / brand headline
    displayLarge = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Black),
    // No.8 - 32 ExtraBold
    displayMedium = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold),
    // No.2 - 32 Bold
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
    // No.1 - 24 SemiBold
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
    // No.3 - 20 Bold
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    // No.4 - 20 Regular
    titleMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal),
    // No.6 - 16 ExtraBold
    labelLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
    // No.5 - 15 SemiBold
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
)
