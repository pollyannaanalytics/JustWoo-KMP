package com.pollyannawu.justwoo.android.ui.house

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.common.JustWooLogo
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.design.DesignTokens

@Composable
fun HouseOnboardingScreen(
    onJoinClick: () -> Unit,
    onCreateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
            .padding(horizontal = JustWooSpacing.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        JustWooLogo()
        Spacer(Modifier.height(JustWooSpacing.Large))
        Text(
            text = "Your House",
            color = JustWooColors.Primary,
            fontSize = DesignTokens.FontSize.Display.sp,
            fontWeight = JustWooFontWeight.Bold,
        )
        Spacer(Modifier.height(JustWooSpacing.Small))
        Text(
            text = "Join or create a house to get started.",
            color = JustWooColors.TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(JustWooSpacing.XXLarge))
        JustWooPrimaryButton(
            text = "Join a House",
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(JustWooSpacing.Default))
        JustWooPrimaryButton(
            text = "Create a House",
            onClick = onCreateClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun HouseOnboardingScreenPreview() {
    HouseOnboardingScreen(
        onJoinClick = {},
        onCreateClick = {},
    )
}
