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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.common.componentViewModelStoreOwner
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.core.dto.EmailInvitationResponse
import com.pollyannawu.justwoo.design.DesignTokens
import com.arkivanov.decompose.ComponentContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import org.koin.androidx.compose.koinViewModel

@Composable
fun PendingInvitationsScreen(
    componentContext: ComponentContext,
    onBack: () -> Unit,
    onJoinSuccess: () -> Unit,
    viewModel: PendingInvitationsViewModel = koinViewModel(
        viewModelStoreOwner = componentViewModelStoreOwner(componentContext),
    ),
) {
    val listState by viewModel.uiState.collectAsState()
    val joinStates by viewModel.joinStates.collectAsState()

    LaunchedEffect(joinStates) {
        if (joinStates.values.any { it.joined }) {
            onJoinSuccess()
        }
    }

    PendingInvitationsContent(
        listState = listState,
        joinStateFor = { invitationId -> joinStates[invitationId] ?: PendingInvitationsViewModel.JoinUiState() },
        onCodeChange = viewModel::onCodeChange,
        onJoin = viewModel::join,
        onRetry = viewModel::retry,
        onBack = onBack,
    )
}

@Composable
private fun PendingInvitationsContent(
    listState: PendingInvitationsViewModel.ListUiState,
    joinStateFor: (Long) -> PendingInvitationsViewModel.JoinUiState,
    onCodeChange: (Long, String) -> Unit,
    onJoin: (Long) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
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
            text = "My Invitations",
            color = JustWooColors.Primary,
            fontSize = DesignTokens.FontSize.TitleLarge.sp,
            fontWeight = JustWooFontWeight.Bold,
        )
        Spacer(Modifier.height(JustWooSpacing.Large))

        when (listState) {
            PendingInvitationsViewModel.ListUiState.Loading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = JustWooColors.Primary)
            }

            PendingInvitationsViewModel.ListUiState.Empty -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "You have no pending invitations.",
                    color = JustWooColors.TextSecondary,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            is PendingInvitationsViewModel.ListUiState.Error -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = listState.message ?: "Something went wrong. Please try again.",
                        color = JustWooColors.Error,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(JustWooSpacing.Large))
                    JustWooPrimaryButton(
                        text = "Retry",
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            is PendingInvitationsViewModel.ListUiState.Invitations -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Default),
            ) {
                items(listState.invitations, key = { it.id }) { invitation ->
                    InvitationCard(
                        invitation = invitation,
                        joinState = joinStateFor(invitation.id),
                        onCodeChange = { value -> onCodeChange(invitation.id, value) },
                        onJoin = { onJoin(invitation.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun InvitationCard(
    invitation: EmailInvitationResponse,
    joinState: PendingInvitationsViewModel.JoinUiState,
    onCodeChange: (String) -> Unit,
    onJoin: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JustWooColors.CreamSurface),
    ) {
        Column(modifier = Modifier.padding(JustWooSpacing.Default)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(JustWooColors.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = invitation.houseName.take(1).uppercase(),
                        color = JustWooColors.OnPrimary,
                        fontWeight = JustWooFontWeight.Bold,
                        modifier = Modifier.padding(horizontal = JustWooSpacing.Small),
                    )
                }
                Spacer(Modifier.height(0.dp))
                Column(modifier = Modifier.padding(start = JustWooSpacing.Default)) {
                    Text(
                        text = invitation.houseName,
                        color = JustWooColors.TextPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = JustWooFontWeight.Bold,
                    )
                    Text(
                        text = "Expires ${invitation.expiresAt.toReadableDate()}",
                        color = JustWooColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(Modifier.height(JustWooSpacing.Default))
            JustWooTextField(
                value = joinState.codeInput,
                onValueChange = onCodeChange,
                placeholder = "Invite code",
                isError = joinState.joinError != null,
                errorMessage = joinState.joinError,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(JustWooSpacing.Default))
            JustWooPrimaryButton(
                text = "Join",
                onClick = onJoin,
                loading = joinState.joining,
                enabled = joinState.codeInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun Instant.toReadableDate(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.year}-${local.monthNumber.toString().padStart(2, '0')}-${local.dayOfMonth.toString().padStart(2, '0')}"
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun PendingInvitationsLoadingPreview() {
    PendingInvitationsContent(
        listState = PendingInvitationsViewModel.ListUiState.Loading,
        joinStateFor = { PendingInvitationsViewModel.JoinUiState() },
        onCodeChange = { _, _ -> },
        onJoin = {},
        onRetry = {},
        onBack = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun PendingInvitationsEmptyPreview() {
    PendingInvitationsContent(
        listState = PendingInvitationsViewModel.ListUiState.Empty,
        joinStateFor = { PendingInvitationsViewModel.JoinUiState() },
        onCodeChange = { _, _ -> },
        onJoin = {},
        onRetry = {},
        onBack = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun PendingInvitationsErrorPreview() {
    PendingInvitationsContent(
        listState = PendingInvitationsViewModel.ListUiState.Error("Network error"),
        joinStateFor = { PendingInvitationsViewModel.JoinUiState() },
        onCodeChange = { _, _ -> },
        onJoin = {},
        onRetry = {},
        onBack = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 880)
@Composable
private fun PendingInvitationsListPreview() {
    val sample = listOf(
        EmailInvitationResponse(
            id = 1L,
            houseId = 10L,
            houseName = "Sunset Villa",
            houseAvatar = "",
            code = "ABCD1234",
            expiresAt = Clock.System.now() + 5.days,
        ),
        EmailInvitationResponse(
            id = 2L,
            houseId = 11L,
            houseName = "Maple House",
            houseAvatar = "",
            code = "WXYZ5678",
            expiresAt = Clock.System.now() + 2.days,
        ),
    )
    PendingInvitationsContent(
        listState = PendingInvitationsViewModel.ListUiState.Invitations(sample),
        joinStateFor = { PendingInvitationsViewModel.JoinUiState() },
        onCodeChange = { _, _ -> },
        onJoin = {},
        onRetry = {},
        onBack = {},
    )
}
