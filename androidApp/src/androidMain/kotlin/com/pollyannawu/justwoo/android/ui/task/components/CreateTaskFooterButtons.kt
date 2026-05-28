package com.pollyannawu.justwoo.android.ui.task.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.design.DesignTokens

@Composable
fun CreateTaskFooterButtons(
    isSaving: Boolean,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = JustWooSpacing.XLarge)
            .padding(bottom = JustWooSpacing.XLarge),
        horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.Default),
    ) {
        FooterButton(
            text = "Cancel",
            background = JustWooColors.Outline,
            contentColor = JustWooColors.TextPrimary,
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        )
        FooterButton(
            text = if (isSaving) "Saving…" else "Done",
            background = JustWooColors.Primary,
            contentColor = JustWooColors.OnPrimary,
            enabled = !isSaving,
            onClick = onDone,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FooterButton(
    text: String,
    background: Color,
    contentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(JustWooShapes.Medium)
            .background(if (enabled) background else background.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = JustWooFontWeight.ExtraBold,
            fontSize = DesignTokens.FontSize.Label.sp,
        )
    }
}

@Preview(name = "Idle", showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun CreateTaskFooterButtonsIdlePreview() {
    JustWooTheme {
        CreateTaskFooterButtons(isSaving = false, onCancel = {}, onDone = {})
    }
}

@Preview(name = "Saving", showBackground = true, backgroundColor = 0xFFF5F1E7)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CreateTaskFooterButtonsSavingPreview() {
    JustWooTheme {
        CreateTaskFooterButtons(isSaving = true, onCancel = {}, onDone = {})
    }
}
