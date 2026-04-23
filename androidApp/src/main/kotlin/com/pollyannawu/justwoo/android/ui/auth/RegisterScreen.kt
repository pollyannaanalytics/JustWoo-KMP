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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.common.JustWooLogo
import com.pollyannawu.justwoo.android.ui.common.JustWooPasswordField
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(72.dp))
        JustWooLogo()
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Create account",
            color = JustWooColors.Primary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(40.dp))

        JustWooTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "E-mail",
            isError = state.emailError != null,
            errorMessage = state.emailError,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        JustWooPasswordField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Password",
            showPassword = state.showPassword,
            isError = state.passwordError != null,
            errorMessage = state.passwordError,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        JustWooPasswordField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmChange,
            placeholder = "Confirm password",
            showPassword = state.showPassword,
            isError = state.confirmError != null,
            errorMessage = state.confirmError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleShowPassword() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.showPassword,
                onCheckedChange = { viewModel.toggleShowPassword() },
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

        Spacer(Modifier.height(32.dp))

        JustWooPrimaryButton(
            text = "Create account",
            onClick = viewModel::submit,
            enabled = state.canSubmit,
            loading = state.loading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Already have an account?",
                color = JustWooColors.TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Sign in",
                color = JustWooColors.PrimaryDeep,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToSignIn() }
            )
        }
    }
}
