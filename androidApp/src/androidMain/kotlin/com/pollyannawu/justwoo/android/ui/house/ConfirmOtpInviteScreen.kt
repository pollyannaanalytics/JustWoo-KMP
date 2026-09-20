package com.pollyannawu.justwoo.android.ui.house

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.ui.nav.house.ConfirmOtpInviteComponent
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConfirmOtpInviteScreen(
    component: ConfirmOtpInviteComponent,
    modifier: Modifier = Modifier,
) {
    val viewModel: ConfirmOtpInviteViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    var code by remember { mutableStateOf("") }
    var displayNumber by remember { mutableIntStateOf(10) }

    ConfirmOtpInviteContent(
        state = state,
        code = code,
        displayNumber = displayNumber,
        onCodeChange = { newCode ->
            if (newCode.length <= 6 && newCode.all { it.isDigit() }) {
                code = newCode
            }
        },
        onDisplayNumberChange = { displayNumber = it },
        onConfirm = { viewModel.confirm(code, displayNumber) },
        onBack = component::onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmOtpInviteContent(
    state: ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState,
    code: String,
    displayNumber: Int,
    onCodeChange: (String) -> Unit,
    onDisplayNumberChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Invite Code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JustWooColors.Cream)
                .padding(innerPadding)
                .padding(horizontal = JustWooSpacing.XXLarge),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                is ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Loading -> {
                    CircularProgressIndicator(color = JustWooColors.Primary)
                }

                is ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Success -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Invite confirmed!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = JustWooColors.Primary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(JustWooSpacing.Default))
                        Text(
                            text = "The member has been invited to your house.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = JustWooColors.TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                is ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Idle,
                is ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Error -> {
                    ConfirmOtpInviteForm(
                        state = state,
                        code = code,
                        displayNumber = displayNumber,
                        onCodeChange = onCodeChange,
                        onDisplayNumberChange = onDisplayNumberChange,
                        onConfirm = onConfirm,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmOtpInviteForm(
    state: ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState,
    code: String,
    displayNumber: Int,
    onCodeChange: (String) -> Unit,
    onDisplayNumberChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorMessage = (state as? ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Error)?.message
    val isConfirmEnabled = code.length == 6

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Default),
    ) {
        Text(
            text = "Enter the 6-digit code shown on the invitee's phone.",
            style = MaterialTheme.typography.bodyLarge,
            color = JustWooColors.TextSecondary,
            textAlign = TextAlign.Center,
        )

        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            label = { Text("6-digit code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "Select the 2-digit display number shown on the invitee's phone:",
            style = MaterialTheme.typography.bodyMedium,
            color = JustWooColors.TextSecondary,
            textAlign = TextAlign.Center,
        )

        DisplayNumberPicker(
            value = displayNumber,
            onValueChange = onDisplayNumberChange,
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = onConfirm,
            enabled = isConfirmEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Confirm Invite")
        }
    }
}

@Composable
private fun DisplayNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        IconButton(
            onClick = { if (value > 10) onValueChange(value - 1) },
            enabled = value > 10,
        ) {
            Text(text = "<", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.width(JustWooSpacing.Default))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = JustWooColors.Primary,
            modifier = Modifier.width(JustWooSpacing.XXLarge),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(JustWooSpacing.Default))
        IconButton(
            onClick = { if (value < 99) onValueChange(value + 1) },
            enabled = value < 99,
        ) {
            Text(text = ">", style = MaterialTheme.typography.titleLarge)
        }
    }
}

// --- Previews ---

@Preview(name = "Idle - Light", showBackground = true)
@Preview(name = "Idle - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ConfirmOtpInviteIdlePreview() {
    JustWooTheme {
        ConfirmOtpInviteContent(
            state = ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Idle,
            code = "",
            displayNumber = 42,
            onCodeChange = {},
            onDisplayNumberChange = {},
            onConfirm = {},
            onBack = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun ConfirmOtpInviteLoadingPreview() {
    JustWooTheme {
        ConfirmOtpInviteContent(
            state = ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Loading,
            code = "123456",
            displayNumber = 42,
            onCodeChange = {},
            onDisplayNumberChange = {},
            onConfirm = {},
            onBack = {},
        )
    }
}

@Preview(name = "Success", showBackground = true)
@Composable
private fun ConfirmOtpInviteSuccessPreview() {
    JustWooTheme {
        ConfirmOtpInviteContent(
            state = ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Success,
            code = "123456",
            displayNumber = 42,
            onCodeChange = {},
            onDisplayNumberChange = {},
            onConfirm = {},
            onBack = {},
        )
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun ConfirmOtpInviteErrorPreview() {
    JustWooTheme {
        ConfirmOtpInviteContent(
            state = ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Error("Invalid or expired code"),
            code = "000000",
            displayNumber = 42,
            onCodeChange = {},
            onDisplayNumberChange = {},
            onConfirm = {},
            onBack = {},
        )
    }
}
