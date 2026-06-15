package com.pollyannawu.justwoo.android.ui.settlement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.pollyannawu.justwoo.android.ui.common.ComponentViewModelStoreOwner
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.ui.nav.settlement.AddExpenseComponent
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    component: AddExpenseComponent,
    viewModel: AddExpenseViewModel = koinViewModel(
        viewModelStoreOwner = remember(component) { ComponentViewModelStoreOwner(component) },
    ),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            component.onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream),
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
                text = "Add Expense",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = JustWooFontWeight.Bold,
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = JustWooSpacing.XLarge, vertical = JustWooSpacing.Small),
        ) {
            item { Text("Amount", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                JustWooTextField(
                    value = state.amount,
                    onValueChange = viewModel::onAmountChange,
                    placeholder = "0.00",
                    isError = state.error != null,
                    errorMessage = state.error,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Decimal,
                )
            }

            item { Spacer(Modifier.height(JustWooSpacing.Default)) }
            item { Text("Currency", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                Box {
                    OutlinedTextField(
                        value = state.currencyCode,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = JustWooColors.TextSecondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { component.onOpenCurrencyPicker { code -> viewModel.onCurrencyChange(code) } },
                    )
                }
            }

            item { Spacer(Modifier.height(JustWooSpacing.Default)) }
            item { Text("Paid by", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                var expanded by remember { mutableStateOf(false) }
                val selectedLabel = state.allMembers
                    .firstOrNull { it.userId == state.selectedPayerId }
                    ?.let { m ->
                        val displayName = m.name.ifBlank { "User #${m.userId}" }
                        if (m.userId == state.currentUserId) "You ($displayName)" else displayName
                    }
                    ?: "..."
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        state.allMembers.forEach { member ->
                            val displayName = member.name.ifBlank { "User #${member.userId}" }
                            val label = if (member.userId == state.currentUserId) "You ($displayName)" else displayName
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { viewModel.onPayerSelect(member.userId); expanded = false },
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(JustWooSpacing.Default)) }
            item { Text("Payee", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                var expanded by remember { mutableStateOf(false) }
                val selectedLabel = state.payeeMembers
                    .firstOrNull { it.userId == state.selectedPayeeId }
                    ?.name
                    ?: "House (split equally)"

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("House (split equally)") },
                            onClick = { viewModel.onPayeeSelect(null); expanded = false },
                        )
                        state.payeeMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name.ifBlank { "User #${member.userId}" }) },
                                onClick = { viewModel.onPayeeSelect(member.userId); expanded = false },
                            )
                        }
                    }
                }
                if (state.selectedPayeeId == null && state.payeeMembers.size > 1) {
                    Spacer(Modifier.height(JustWooSpacing.XSmall))
                    Text(
                        "Amount will be split equally among ${state.payeeMembers.size} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = JustWooColors.TextSecondary,
                    )
                }
            }

            item { Spacer(Modifier.height(JustWooSpacing.Default)) }
            item { Text("Note (optional)", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                JustWooTextField(
                    value = state.note,
                    onValueChange = viewModel::onNoteChange,
                    placeholder = "What was this for?",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(JustWooSpacing.XLarge * 4),
                )
            }

            if (state.partialFailureIds.isNotEmpty()) {
                item { Spacer(Modifier.height(JustWooSpacing.Default)) }
                item {
                    Text(
                        "Could not create records for ${state.partialFailureIds.size} member(s): ${state.partialFailureIds.joinToString()}",
                        color = JustWooColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            item { Spacer(Modifier.height(JustWooSpacing.Large)) }
            item {
                JustWooPrimaryButton(
                    text = if (state.isLoading) "Saving…" else "Save",
                    onClick = viewModel::submit,
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item { Spacer(Modifier.height(JustWooSpacing.Large)) }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AddExpensePreview() {
    MaterialTheme {
        Text("Add Expense Preview", modifier = Modifier.padding(JustWooSpacing.Default))
    }
}
