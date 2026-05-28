package com.pollyannawu.justwoo.android.ui.house

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.core.dto.InviteCodeResponse
import com.pollyannawu.justwoo.design.DesignTokens
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun GenerateInviteCodeSheet(
    viewModel: GenerateInviteCodeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    GenerateInviteCodeContent(
        state = state,
        isAdmin = isAdmin,
        onGenerateClick = viewModel::generateCode,
    )
}

@Composable
private fun GenerateInviteCodeContent(
    state: GenerateInviteCodeViewModel.UiState,
    isAdmin: Boolean,
    onGenerateClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(JustWooSpacing.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Invite Member",
            color = JustWooColors.Primary,
            fontSize = DesignTokens.FontSize.TitleLarge.sp,
            fontWeight = JustWooFontWeight.Bold,
        )
        Spacer(Modifier.height(JustWooSpacing.Large))

        if (!isAdmin) {
            Text(
                text = "Only house admins can generate invite codes.",
                color = JustWooColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            return
        }

        if (state.generatedCode != null) {
            Text(
                text = "Share this code:",
                color = JustWooColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(JustWooSpacing.Small))
            Text(
                text = state.generatedCode.code,
                color = JustWooColors.Primary,
                fontSize = 36.sp,
                fontWeight = JustWooFontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Spacer(Modifier.height(JustWooSpacing.Small))
            Text(
                text = "Expires: ${state.generatedCode.expiresAt.formatExpiry()}",
                color = JustWooColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(JustWooSpacing.Large))
        }

        if (state.error != null) {
            Text(
                text = state.error,
                color = JustWooColors.Error,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(JustWooSpacing.Small))
        }

        JustWooPrimaryButton(
            text = if (state.generatedCode == null) "Generate Code" else "Generate New Code",
            onClick = onGenerateClick,
            loading = state.loading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun Instant.formatExpiry(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d".format(local.hour, local.minute)
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun GenerateInviteCodeSheetPreview() {
    GenerateInviteCodeContent(
        state = GenerateInviteCodeViewModel.UiState(),
        isAdmin = true,
        onGenerateClick = {},
    )
}
