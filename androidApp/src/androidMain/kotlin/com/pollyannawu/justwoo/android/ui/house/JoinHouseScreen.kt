package com.pollyannawu.justwoo.android.ui.house

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.design.DesignTokens
import com.pollyannawu.justwoo.android.ui.common.componentViewModelStoreOwner
import com.pollyannawu.justwoo.ui.nav.house.HouseOnboardingComponent
import org.koin.androidx.compose.koinViewModel

@Composable
fun JoinHouseScreen(
    component: HouseOnboardingComponent,
    viewModel: JoinHouseViewModel = koinViewModel(viewModelStoreOwner = componentViewModelStoreOwner(component)),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            is JoinHouseViewModel.JoinUiState.Approved -> component.onCompleted()
            else -> Unit
        }
    }

    JoinHouseContent(
        state = state,
        onCodeChange = viewModel::onCodeChange,
        onSubmit = viewModel::submit,
        onBack = component::onBack,
    )
}

@Composable
private fun JoinHouseContent(
    state: JoinHouseViewModel.JoinUiState,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
            .padding(horizontal = JustWooSpacing.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(72.dp))
        Text(
            text = "Join a House",
            color = JustWooColors.Primary,
            fontSize = DesignTokens.FontSize.Display.sp,
            fontWeight = JustWooFontWeight.Bold,
        )
        Spacer(Modifier.height(JustWooSpacing.XXLarge))

        when (state) {
            is JoinHouseViewModel.JoinUiState.EnteringCode -> {
                JustWooTextField(
                    value = state.code,
                    onValueChange = onCodeChange,
                    placeholder = "Invite code",
                    isError = state.error != null,
                    errorMessage = state.error,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(JustWooSpacing.XLarge))
                JustWooPrimaryButton(
                    text = "Submit",
                    onClick = onSubmit,
                    loading = state.loading,
                    enabled = state.code.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            JoinHouseViewModel.JoinUiState.WaitingApproval -> {
                CircularProgressIndicator(color = JustWooColors.Primary)
                Spacer(Modifier.height(JustWooSpacing.Large))
                Text(
                    text = "Waiting for admin approval…",
                    color = JustWooColors.TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(JustWooSpacing.Small))
                Text(
                    text = "This page will update automatically.",
                    color = JustWooColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            JoinHouseViewModel.JoinUiState.Approved -> {
                // navigates automatically via LaunchedEffect in JoinHouseScreen
            }

            is JoinHouseViewModel.JoinUiState.Rejected -> {
                Text(
                    text = state.message,
                    color = JustWooColors.Error,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(JustWooSpacing.XLarge))
                JustWooPrimaryButton(
                    text = "Back to selector",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun JoinHouseEnteringCodePreview() {
    JoinHouseContent(
        state = JoinHouseViewModel.JoinUiState.EnteringCode(code = "ABC123"),
        onCodeChange = {},
        onSubmit = {},
        onBack = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun JoinHouseWaitingPreview() {
    JoinHouseContent(
        state = JoinHouseViewModel.JoinUiState.WaitingApproval,
        onCodeChange = {},
        onSubmit = {},
        onBack = {},
    )
}
