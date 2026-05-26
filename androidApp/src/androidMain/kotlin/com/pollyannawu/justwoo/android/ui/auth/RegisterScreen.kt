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
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.success) {
        if (state.success) {
            viewModel.consumeSuccess()
            onRegisterSuccess()
        }
    }

    RegisterContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmChange = viewModel::onConfirmChange,
        onToggleShowPassword = viewModel::toggleShowPassword,
        onRegisterClick = viewModel::submit,
        onNavigateToSignIn = onNavigateToSignIn
    )
}

@Composable
private fun RegisterContent(
    state: RegisterViewModel.UiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onToggleShowPassword: () -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToSignIn: () -> Unit,
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
            text = "Sign up",
            color = JustWooColors.Primary,
            fontSize = DesignTokens.FontSize.Display.sp,
            fontWeight = JustWooFontWeight.Bold,
        )

        Spacer(Modifier.height(JustWooSpacing.Default))

        Text(
            text = "Please set your password.",
            color = JustWooColors.TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(Modifier.height(40.dp))

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
        if (state.passwordError == null) {
            Text(
                text = "*Required at least ${RegisterViewModel.MIN_PASSWORD_LENGTH} characters",
                color = JustWooColors.TextPrimary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = JustWooSpacing.Default, top = JustWooSpacing.XSmall)
            )
        }
        Spacer(Modifier.height(JustWooSpacing.Large))
        JustWooPasswordField(
            value = state.confirmPassword,
            onValueChange = onConfirmChange,
            placeholder = "Confirm password",
            showPassword = state.showPassword,
            isError = state.confirmError != null,
            errorMessage = state.confirmError,
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

        Spacer(Modifier.height(JustWooSpacing.XXLarge))

        JustWooPrimaryButton(
            text = "Join !",
            onClick = onRegisterClick,
            enabled = state.canSubmit,
            loading = state.loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(JustWooSpacing.Large))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Already have an account?",
                color = JustWooColors.TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.width(JustWooSpacing.Small))
            Text(
                text = "Sign in",
                color = JustWooColors.PrimaryDeep,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = JustWooFontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToSignIn() }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun RegisterContentPreview() {
    RegisterContent(
        state = RegisterViewModel.UiState(),
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmChange = {},
        onToggleShowPassword = {},
        onRegisterClick = {},
        onNavigateToSignIn = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun RegisterContentErrorPreview() {
    RegisterContent(
        state = RegisterViewModel.UiState(
            email = "abc@",
            password = "short",
            confirmPassword = "no-match",
            passwordError = "Required at least 10 characters",
            confirmError = "Passwords do not match.",
        ),
        onEmailChange = {},
        onPasswordChange = {},
        onConfirmChange = {},
        onToggleShowPassword = {},
        onRegisterClick = {},
        onNavigateToSignIn = {},
    )
}
