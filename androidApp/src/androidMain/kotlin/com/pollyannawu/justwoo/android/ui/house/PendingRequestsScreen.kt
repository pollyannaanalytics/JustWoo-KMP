package com.pollyannawu.justwoo.android.ui.house

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.core.JoinRequestStatus
import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import com.pollyannawu.justwoo.design.DesignTokens
import kotlinx.datetime.Clock
import org.koin.androidx.compose.koinViewModel

@Composable
fun PendingRequestsScreen(
    viewModel: PendingRequestsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PendingRequestsViewModel.Event.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = JustWooColors.Cream,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = JustWooColors.PrimaryDeep)
            }
        },
    ) { padding ->
        PendingRequestsContent(
            state = state,
            onApprove = viewModel::approve,
            onReject = viewModel::reject,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun PendingRequestsContent(
    state: PendingRequestsViewModel.UiState,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
            .padding(horizontal = JustWooSpacing.Large),
    ) {
        Spacer(Modifier.height(JustWooSpacing.Large))
        Text(
            text = "Pending Requests",
            color = JustWooColors.Primary,
            fontSize = DesignTokens.FontSize.TitleLarge.sp,
            fontWeight = JustWooFontWeight.Bold,
        )
        Spacer(Modifier.height(JustWooSpacing.Large))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = JustWooColors.Primary)
            }
            state.requests.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No pending requests.",
                    color = JustWooColors.TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Default)) {
                items(state.requests, key = { it.id }) { request ->
                    PendingRequestRow(
                        request = request,
                        onApprove = { onApprove(request.id) },
                        onReject = { onReject(request.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingRequestRow(
    request: JoinRequestResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JustWooColors.CreamSurface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(JustWooSpacing.Default),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "User #${request.userId}",
                color = JustWooColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Row {
                TextButton(onClick = onApprove) {
                    Text("Approve", color = JustWooColors.AccentMint, fontWeight = JustWooFontWeight.Bold)
                }
                TextButton(onClick = onReject) {
                    Text("Reject", color = JustWooColors.Error, fontWeight = JustWooFontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun PendingRequestsContentPreview() {
    val now = Clock.System.now()
    PendingRequestsContent(
        state = PendingRequestsViewModel.UiState(
            requests = listOf(
                JoinRequestResponse(1L, 10L, 101L, JoinRequestStatus.PENDING, now),
                JoinRequestResponse(2L, 10L, 102L, JoinRequestStatus.PENDING, now),
            )
        ),
        onApprove = {},
        onReject = {},
    )
}
