package com.pollyannawu.justwoo.android.ui.house

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteByEmailScreen(
    houseId: Long,
    onBack: () -> Unit,
    viewModel: InviteByEmailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite by Email") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(JustWooColors.Cream)
                .padding(padding)
                .padding(horizontal = JustWooSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Medium),
        ) {
            Spacer(Modifier.height(JustWooSpacing.Large))

            when (val state = uiState) {
                is InviteByEmailViewModel.UiState.EnteringEmail -> {
                    Text(
                        text = "Enter the email address of the person you want to invite.",
                        fontSize = 14.sp,
                        color = JustWooColors.TextSecondary,
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Email address") },
                        singleLine = true,
                        isError = state.error != null,
                        supportingText = state.error?.let { { Text(it, color = JustWooColors.Error) } },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { viewModel.sendInvite(houseId) },
                        enabled = !state.loading && state.email.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Send Invite")
                        }
                    }
                }

                is InviteByEmailViewModel.UiState.CodeGenerated -> {
                    Text(
                        text = "Invitation created for ${state.houseName}!",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = "Share this code with your invitee. It expires in 7 days.",
                        fontSize = 14.sp,
                        color = JustWooColors.TextSecondary,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, JustWooColors.Primary, RoundedCornerShape(8.dp))
                            .padding(horizontal = JustWooSpacing.Medium, vertical = JustWooSpacing.Large),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = state.code,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = JustWooColors.Primary,
                            letterSpacing = 4.sp,
                        )
                        TextButton(onClick = {
                            clipboard.setText(AnnotatedString(state.code))
                            scope.launch { snackbarHostState.showSnackbar("Code copied!") }
                        }) {
                            Text("Copy", color = JustWooColors.Primary)
                        }
                    }
                    Spacer(Modifier.height(JustWooSpacing.Medium))
                    Button(
                        onClick = viewModel::reset,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Invite Another Person")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7)
@Composable
private fun InviteByEmailScreenEnteringPreview() {
    JustWooTheme {
        InviteByEmailScreen(houseId = 1L, onBack = {})
    }
}
