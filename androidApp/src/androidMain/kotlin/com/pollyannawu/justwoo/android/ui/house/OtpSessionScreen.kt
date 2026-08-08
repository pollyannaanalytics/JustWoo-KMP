package com.pollyannawu.justwoo.android.ui.house

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.core.dto.OtpInviteSession
import com.pollyannawu.justwoo.ui.nav.house.OtpSessionComponent
import org.koin.androidx.compose.koinViewModel

@Composable
fun OtpSessionScreen(
    component: OtpSessionComponent,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtpSessionViewModel = koinViewModel()
    val state by viewModel.uiState.collectAsState()

    OtpSessionContent(
        state = state,
        onBack = component::onBack,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OtpSessionContent(
    state: OtpSessionViewModel.OtpSessionUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Show My Code") },
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
                is OtpSessionViewModel.OtpSessionUiState.Loading -> {
                    CircularProgressIndicator(color = JustWooColors.Primary)
                }

                is OtpSessionViewModel.OtpSessionUiState.Success -> {
                    OtpSessionSuccessBody(
                        state = state,
                        onRefresh = onRefresh,
                    )
                }

                is OtpSessionViewModel.OtpSessionUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message ?: "Something went wrong. Please try again.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(JustWooSpacing.Default))
                        Button(onClick = onRefresh) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpSessionSuccessBody(
    state: OtpSessionViewModel.OtpSessionUiState.Success,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Default),
    ) {
        Text(
            text = "Invite Code",
            style = MaterialTheme.typography.titleMedium,
            color = JustWooColors.TextSecondary,
        )
        Text(
            text = state.session.code.let {
                if (it.length == 6) "${it.take(3)} ${it.drop(3)}" else it
            },
            fontSize = 48.sp,
            fontFamily = FontFamily.Monospace,
            color = JustWooColors.Primary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Number: ${state.session.displayNumber}",
            style = MaterialTheme.typography.bodyLarge,
            color = JustWooColors.TextSecondary,
        )
        Text(
            text = "Expires in ${state.remainingSeconds}s",
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.remainingSeconds <= 10) MaterialTheme.colorScheme.error else JustWooColors.TextSecondary,
        )
        Spacer(Modifier.height(JustWooSpacing.Small))
        Button(
            onClick = onRefresh,
            enabled = state.canRefresh,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Refresh")
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OtpSessionScreenSuccessPreview() {
    JustWooTheme {
        OtpSessionContent(
            state = OtpSessionViewModel.OtpSessionUiState.Success(
                session = OtpInviteSession(code = "482931", displayNumber = 7, expiresInSeconds = 60),
                remainingSeconds = 47,
            ),
            onBack = {},
            onRefresh = {},
        )
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun OtpSessionScreenLoadingPreview() {
    JustWooTheme {
        OtpSessionContent(
            state = OtpSessionViewModel.OtpSessionUiState.Loading,
            onBack = {},
            onRefresh = {},
        )
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun OtpSessionScreenErrorPreview() {
    JustWooTheme {
        OtpSessionContent(
            state = OtpSessionViewModel.OtpSessionUiState.Error("Network failure"),
            onBack = {},
            onRefresh = {},
        )
    }
}
