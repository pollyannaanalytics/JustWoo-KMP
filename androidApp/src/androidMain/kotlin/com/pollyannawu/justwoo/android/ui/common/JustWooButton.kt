package com.pollyannawu.justwoo.android.ui.common

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors

/**
 * Yellow pill primary button matching the "Sign in" CTA in Figma.
 */
@Composable
fun JustWooPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = JustWooColors.Primary,
            contentColor = JustWooColors.OnPrimary,
            disabledContainerColor = JustWooColors.Primary.copy(alpha = 0.5f),
            disabledContentColor = JustWooColors.OnPrimary.copy(alpha = 0.8f),
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = JustWooColors.OnPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.height(20.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}
