package com.pollyannawu.justwoo.android.ui.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.dto.BalanceEntry
import com.pollyannawu.justwoo.ui.nav.settlement.SettlementComponent
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettlementOverviewScreen(
    component: SettlementComponent,
    viewModel: SettlementOverviewViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = JustWooColors.Cream,
        floatingActionButton = {
            FloatingActionButton(
                onClick = component::onAddExpense,
                containerColor = JustWooColors.Primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add expense", tint = JustWooColors.OnPrimary)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = JustWooSpacing.XSmall, vertical = JustWooSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = component::onClose) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = JustWooColors.TextPrimary)
                }
                Text(
                    text = "Settlement",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = JustWooFontWeight.Bold,
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = JustWooSpacing.XLarge,
                    vertical = JustWooSpacing.Small,
                ),
                verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Small),
            ) {
                item {
                    Text("Balances", style = MaterialTheme.typography.titleMedium, fontWeight = JustWooFontWeight.SemiBold)
                    Spacer(Modifier.height(JustWooSpacing.Small))
                }

                item {
                    BalanceSection(
                        isLoading = state.isBalanceLoading,
                        error = state.balanceError,
                        entries = state.balanceEntries,
                    )
                    Spacer(Modifier.height(JustWooSpacing.Large))
                }

                item {
                    Text("History", style = MaterialTheme.typography.titleMedium, fontWeight = JustWooFontWeight.SemiBold)
                    Spacer(Modifier.height(JustWooSpacing.Small))
                }

                if (state.settlements.isEmpty()) {
                    item {
                        Text(
                            "No settlements yet.",
                            color = JustWooColors.TextSecondary,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                } else {
                    items(state.settlements, key = { it.id }) { settlement ->
                        SettlementHistoryItem(settlement)
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun BalanceSection(
    isLoading: Boolean,
    error: String?,
    entries: List<BalanceEntry>,
) {
    when {
        isLoading -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = JustWooColors.Primary)
        }
        error != null -> Text(
            text = error,
            color = JustWooColors.Error,
            style = MaterialTheme.typography.bodyMedium,
        )
        entries.isEmpty() -> Text(
            "All settled up!",
            color = JustWooColors.TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
        else -> Column(verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Small)) {
            entries.forEach { entry -> BalanceRow(entry) }
        }
    }
}

@Composable
private fun BalanceRow(entry: BalanceEntry) {
    val isSettled = entry.netAmountTwd == 0.0
    val isOwed = entry.netAmountTwd < 0.0

    val (label, amount, labelColor, bgColor) = when {
        isSettled -> BalanceRowStyle("All settled", "—", JustWooColors.TextSecondary, JustWooColors.CreamSurface)
        isOwed -> BalanceRowStyle(
            "You are owed",
            "${String.format("%.2f", -entry.netAmountTwd)} ${entry.currencyCode}",
            JustWooColors.UrgencyGreen,
            JustWooColors.UrgencyGreenBg,
        )
        else -> BalanceRowStyle(
            "You owe",
            "${String.format("%.2f", entry.netAmountTwd)} ${entry.currencyCode}",
            JustWooColors.UrgencyRed,
            JustWooColors.UrgencyRedBg,
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(JustWooShapes.Medium)
            .background(bgColor)
            .padding(horizontal = JustWooSpacing.Default, vertical = JustWooSpacing.Default),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(entry.counterpartName, style = MaterialTheme.typography.bodyLarge, fontWeight = JustWooFontWeight.SemiBold, color = JustWooColors.TextPrimary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = labelColor)
        }
        Text(amount, style = MaterialTheme.typography.bodyLarge, fontWeight = JustWooFontWeight.Bold, color = labelColor)
    }
}

private data class BalanceRowStyle(
    val label: String,
    val amount: String,
    val labelColor: androidx.compose.ui.graphics.Color,
    val bgColor: androidx.compose.ui.graphics.Color,
)

@Composable
private fun SettlementHistoryItem(settlement: Settlement) {
    val date = settlement.createTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(JustWooShapes.Medium)
            .background(JustWooColors.CreamSurface)
            .padding(horizontal = JustWooSpacing.Default, vertical = JustWooSpacing.Default),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Payer #${settlement.payerId} → Payee #${settlement.payeeId}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = JustWooFontWeight.Medium,
                color = JustWooColors.TextPrimary,
            )
            if (settlement.note.isNotBlank()) {
                Text(settlement.note, style = MaterialTheme.typography.bodySmall, color = JustWooColors.TextSecondary)
            }
            Text("$date", style = MaterialTheme.typography.labelSmall, color = JustWooColors.TextSecondary)
        }
        Text(
            "${String.format("%.2f", settlement.amount)} ${settlement.currencyCode}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = JustWooFontWeight.Bold,
            color = JustWooColors.TextPrimary,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SettlementOverviewPreview() {
    MaterialTheme {
        Box(modifier = Modifier.background(JustWooColors.Cream)) {
            Text("Settlement Overview Preview", modifier = Modifier.padding(16.dp))
        }
    }
}
