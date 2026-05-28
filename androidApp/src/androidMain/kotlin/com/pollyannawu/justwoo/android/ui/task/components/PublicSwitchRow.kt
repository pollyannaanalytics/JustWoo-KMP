package com.pollyannawu.justwoo.android.ui.task.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.design.DesignTokens

@Composable
fun PublicSwitchRow(
    accessLevel: AccessLevel,
    onToggle: (AccessLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Public",
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = DesignTokens.FontSize.Title.sp,
        )
        Switch(
            checked = accessLevel == AccessLevel.PUBLIC,
            onCheckedChange = { checked ->
                onToggle(if (checked) AccessLevel.PUBLIC else AccessLevel.PRIVATE)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = JustWooColors.Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = JustWooColors.Outline,
            ),
        )
    }
}

@Preview(name = "Public ON", showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun PublicSwitchRowOnPreview() {
    JustWooTheme {
        PublicSwitchRow(accessLevel = AccessLevel.PUBLIC, onToggle = {})
    }
}

@Preview(name = "Public OFF", showBackground = true, backgroundColor = 0xFFF5F1E7)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PublicSwitchRowOffPreview() {
    JustWooTheme {
        PublicSwitchRow(accessLevel = AccessLevel.PRIVATE, onToggle = {})
    }
}
