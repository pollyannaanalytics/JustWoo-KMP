package com.pollyannawu.justwoo.android.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.nav.LocalAppActions
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.common.componentViewModelStoreOwner
import com.pollyannawu.justwoo.ui.nav.profile.ProfileViewComponent
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileViewScreen(
    component: ProfileViewComponent,
    viewModel: ProfileViewViewModel = koinViewModel(viewModelStoreOwner = componentViewModelStoreOwner(component)),
) {
    val profile by viewModel.profile.collectAsState()
    val email by viewModel.email.collectAsState()
    val passwordDialog by viewModel.passwordDialog.collectAsState()
    val actions = LocalAppActions.current

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
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = JustWooColors.TextPrimary,
                )
            }
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = JustWooFontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = component::onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit profile",
                    tint = JustWooColors.TextPrimary,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = JustWooSpacing.XLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(JustWooSpacing.Large))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(JustWooColors.UrgencyYellowBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = profile?.name?.take(1)?.uppercase() ?: "?",
                    fontWeight = JustWooFontWeight.Black,
                    style = MaterialTheme.typography.displayMedium,
                    color = JustWooColors.PrimaryDeep,
                )
            }

            Spacer(Modifier.height(JustWooSpacing.Large))

            Text(
                text = profile?.name?.ifBlank { "—" } ?: "—",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = JustWooFontWeight.Bold,
            )

            Spacer(Modifier.height(JustWooSpacing.XLarge))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = JustWooSpacing.XLarge),
        ) {
            val bio = profile?.bio.orEmpty()
            ProfileInfoRow(label = "Bio", value = bio.ifBlank { "—" })
            Spacer(Modifier.height(JustWooSpacing.Small))

            ProfileInfoRow(
                label = "Bank Account",
                value = profile?.bankAccount?.ifBlank { "—" } ?: "—",
            )

            Spacer(Modifier.height(JustWooSpacing.Small))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(JustWooShapes.Medium)
                    .background(JustWooColors.CreamSurface)
                    .padding(horizontal = JustWooSpacing.Default, vertical = JustWooSpacing.Default),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.labelSmall,
                        color = JustWooColors.TextSecondary,
                    )
                    Text(
                        text = email ?: "—",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = JustWooFontWeight.Medium,
                        color = JustWooColors.TextPrimary,
                    )
                }
                IconButton(onClick = viewModel::onChangePasswordClick) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Change password",
                        tint = JustWooColors.TextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(JustWooSpacing.Small))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(JustWooShapes.Medium)
                    .background(JustWooColors.CreamSurface)
                    .clickable { actions.onHouseInfoClick() }
                    .padding(horizontal = JustWooSpacing.Default, vertical = JustWooSpacing.Default),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "House",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = JustWooFontWeight.Medium,
                    color = JustWooColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = JustWooColors.TextSecondary,
                )
            }
        }
    }

    when (val dialog = passwordDialog) {
        is ProfileViewViewModel.PasswordDialogState.Changing -> ChangePasswordDialog(
            state = dialog,
            onDismiss = viewModel::onPasswordDialogDismiss,
            onOldPasswordChange = viewModel::onOldPasswordChange,
            onNewPasswordChange = viewModel::onNewPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onSubmit = viewModel::onSubmitPasswordChange,
        )
        is ProfileViewViewModel.PasswordDialogState.Success -> SuccessDialog(
            onDismiss = viewModel::onPasswordDialogDismiss,
        )
        ProfileViewViewModel.PasswordDialogState.Hidden -> Unit
    }
}

@Composable
private fun ChangePasswordDialog(
    state: ProfileViewViewModel.PasswordDialogState.Changing,
    onDismiss: () -> Unit,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!state.isLoading) onDismiss() },
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = state.oldPassword,
                    onValueChange = onOldPasswordChange,
                    label = { Text("Current password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(JustWooSpacing.Small))
                OutlinedTextField(
                    value = state.newPassword,
                    onValueChange = onNewPasswordChange,
                    label = { Text("New password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(JustWooSpacing.Small))
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text("Confirm new password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    enabled = !state.isLoading,
                    isError = state.error != null,
                    supportingText = state.error?.let { { Text(it, color = JustWooColors.Error) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSubmit,
                enabled = !state.isLoading &&
                    state.oldPassword.isNotBlank() &&
                    state.newPassword.isNotBlank() &&
                    state.confirmPassword.isNotBlank(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isLoading) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun SuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Password Changed") },
        text = { Text("Your password has been updated successfully.") },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
    )
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(JustWooShapes.Medium)
            .background(JustWooColors.CreamSurface)
            .padding(horizontal = JustWooSpacing.Default, vertical = JustWooSpacing.Default),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = JustWooColors.TextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = JustWooFontWeight.Medium,
            color = JustWooColors.TextPrimary,
        )
    }
}
