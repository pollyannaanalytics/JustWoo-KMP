package com.pollyannawu.justwoo.android.ui.house

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.pollyannawu.justwoo.ui.nav.house.HouseOnboardingComponent
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateHouseScreen(
    component: HouseOnboardingComponent,
    viewModel: CreateHouseViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CreateHouseViewModel.Event.NavigateToHome -> component.onCompleted()
            }
        }
    }

    CreateHouseContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onSubmit = viewModel::submit,
    )
}

@Composable
private fun CreateHouseContent(
    state: CreateHouseViewModel.UiState,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
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
            text = "Create a House",
            color = JustWooColors.Primary,
            fontSize = DesignTokens.FontSize.Display.sp,
            fontWeight = JustWooFontWeight.Bold,
        )
        Spacer(Modifier.height(JustWooSpacing.XXLarge))
        JustWooTextField(
            value = state.name,
            onValueChange = onNameChange,
            placeholder = "House name",
            isError = state.nameError != null,
            errorMessage = state.nameError,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.error != null) {
            Spacer(Modifier.height(JustWooSpacing.Small))
            Text(
                text = state.error,
                color = JustWooColors.Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = JustWooSpacing.Default),
            )
        }
        Spacer(Modifier.height(JustWooSpacing.XLarge))
        JustWooPrimaryButton(
            text = "Create",
            onClick = onSubmit,
            loading = state.loading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun CreateHouseScreenPreview() {
    CreateHouseContent(
        state = CreateHouseViewModel.UiState(),
        onNameChange = {},
        onSubmit = {},
    )
}
