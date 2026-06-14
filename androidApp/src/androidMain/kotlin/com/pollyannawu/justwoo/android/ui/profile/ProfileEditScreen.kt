package com.pollyannawu.justwoo.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileEditScreen(
    onClose: () -> Unit,
    viewModel: ProfileEditViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onClose()
        }
    }

    state.saveError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::consumeSaveError,
            title = { Text("Save Failed") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::consumeSaveError) { Text("OK") }
            },
        )
    }

    ProfileEditContent(
        state = state,
        canSave = state.canSave,
        onClose = onClose,
        onNameChange = viewModel::onNameChange,
        onBioChange = viewModel::onBioChange,
        onBankAccountChange = viewModel::onBankAccountChange,
        onSave = viewModel::save,
    )
}

@Composable
private fun ProfileEditContent(
    state: ProfileEditViewModel.UiState,
    canSave: Boolean,
    onClose: () -> Unit,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onBankAccountChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = JustWooSpacing.XSmall, vertical = JustWooSpacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = JustWooColors.TextPrimary,
                )
            }
            Text(
                text = "Edit profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = JustWooFontWeight.Bold,
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = JustWooSpacing.XLarge, vertical = JustWooSpacing.Small)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = JustWooSpacing.Medium),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(JustWooColors.UrgencyYellowBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.name.take(1).uppercase().ifBlank { "W" },
                            fontWeight = JustWooFontWeight.Black,
                            style = MaterialTheme.typography.displayMedium,
                            color = JustWooColors.PrimaryDeep,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(JustWooSpacing.XLarge)) }

            item { Text("Name", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                JustWooTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    placeholder = "Your name",
                    isError = state.nameError != null,
                    errorMessage = state.nameError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { CounterHint(state.name.length, ProfileEditViewModel.NAME_LIMIT) }
            item { Spacer(Modifier.height(JustWooSpacing.Default)) }

            item { Text("Bio", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                JustWooTextField(
                    value = state.bio,
                    onValueChange = onBioChange,
                    placeholder = "A few words about you",
                    isError = state.bioError != null,
                    errorMessage = state.bioError,
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
            item { CounterHint(state.bio.length, ProfileEditViewModel.BIO_LIMIT) }
            item { Spacer(Modifier.height(JustWooSpacing.Default)) }

            item { Text("Bank Account", fontWeight = JustWooFontWeight.SemiBold) }
            item { Spacer(Modifier.height(JustWooSpacing.Small)) }
            item {
                JustWooTextField(
                    value = state.bankAccount,
                    onValueChange = onBankAccountChange,
                    placeholder = "Your bank account number",
                    isError = state.bankAccountError != null,
                    errorMessage = state.bankAccountError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { CounterHint(state.bankAccount.length, ProfileEditViewModel.BANK_ACCOUNT_LIMIT) }

            item { Spacer(Modifier.height(JustWooSpacing.Large)) }
            item {
                JustWooPrimaryButton(
                    text = "Save",
                    onClick = onSave,
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { Spacer(Modifier.height(JustWooSpacing.Large)) }
        }
    }
}


@Composable
private fun CounterHint(current: Int, max: Int) {
    Text(
        text = "$current/$max",
        color = if (current > max) JustWooColors.Error else JustWooColors.TextSecondary,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = JustWooSpacing.Medium, top = 2.dp),
    )
}

