package com.pollyannawu.justwoo.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.pollyannawu.justwoo.android.ui.common.JustWooLogo
import com.pollyannawu.justwoo.android.ui.common.JustWooPasswordField
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.design.DesignTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: SignInViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.consumeSuccess()
            onSignInSuccess()
        }
    }

    SignInContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onToggleShowPassword = viewModel::toggleShowPassword,
        onSignInClick = viewModel::submit,
        onNavigateToRegister = onNavigateToRegister
    )
}

@Composable
private fun SignInContent(
    state: SignInViewModel.UiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleShowPassword: () -> Unit,
    onSignInClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
            .padding(horizontal = JustWooSpacing.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))
        JustWooLogo()
        Spacer(Modifier.height(JustWooSpacing.Default))
        Text(
            text = "Sign in",
            color = JustWooColors.Primary,
            fontSize = DesignTokens.FontSize.Display.sp,
            fontWeight = JustWooFontWeight.Bold,
        )

        Spacer(Modifier.height(48.dp))

        JustWooTextField(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "E-mail",
            isError = state.emailError != null,
            errorMessage = state.emailError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(JustWooSpacing.Large))

        JustWooPasswordField(
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = "Password",
            showPassword = state.showPassword,
            isError = state.passwordError != null,
            errorMessage = state.passwordError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(JustWooSpacing.Small))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleShowPassword() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.showPassword,
                onCheckedChange = { onToggleShowPassword() },
                colors = CheckboxDefaults.colors(
                    checkedColor = JustWooColors.Primary,
                    uncheckedColor = JustWooColors.Outline,
                    checkmarkColor = JustWooColors.OnPrimary,
                )
            )
            Text(
                text = "Show password",
                color = JustWooColors.TextPrimary,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(Modifier.height(40.dp))

        JustWooPrimaryButton(
            text = "Sign in",
            onClick = onSignInClick,
            enabled = state.canSubmit,
            loading = state.loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(JustWooSpacing.XLarge))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "New to JustWoo?",
                color = JustWooColors.TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.width(JustWooSpacing.Small))
            Text(
                text = "Create account",
                color = JustWooColors.PrimaryDeep,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = JustWooFontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun SignInContentPreview() {
    SignInContent(
        state = SignInViewModel.UiState(),
        onEmailChange = {},
        onPasswordChange = {},
        onToggleShowPassword = {},
        onSignInClick = {},
        onNavigateToRegister = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun SignInContentErrorPreview() {
    SignInContent(
        state = SignInViewModel.UiState(
            email = "user@example.com",
            password = "wrong",
            passwordError = "Password incorrect.",
        ),
        onEmailChange = {},
        onPasswordChange = {},
        onToggleShowPassword = {},
        onSignInClick = {},
        onNavigateToRegister = {},
    )
}
