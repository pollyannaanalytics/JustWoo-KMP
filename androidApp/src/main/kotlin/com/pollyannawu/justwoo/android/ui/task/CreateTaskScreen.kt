package com.pollyannawu.justwoo.android.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pollyannawu.justwoo.android.ui.common.JustWooPrimaryButton
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.core.AccessLevel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateTaskScreen(
    currentUserId: Long,
    currentHouseId: Long,
    onClose: () -> Unit,
    viewModel: CreateTaskViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JustWooColors.Cream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Create task",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
        ) {
            JustWooTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = "Task title",
                isError = state.titleError != null,
                errorMessage = state.titleError,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            JustWooTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                placeholder = "Description (optional)",
                singleLine = false,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            Spacer(Modifier.height(16.dp))

            val local = state.dueTime.toLocalDateTime(TimeZone.currentSystemDefault())
            InfoRow(
                label = "Due",
                value = "${local.date} ${local.time.hour.toString().padStart(2, '0')}:${local.time.minute.toString().padStart(2, '0')}",
                onClick = { /* date picker: out of scope for this pass */ }
            )
            if (state.dueDateWarning != null) {
                Text(
                    text = "*${state.dueDateWarning}",
                    color = JustWooColors.Error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            InfoRow(
                label = "Access",
                value = state.accessLevel.name.lowercase().replaceFirstChar { it.titlecase() },
                onClick = {
                    viewModel.onAccessLevelChange(
                        if (state.accessLevel == AccessLevel.PUBLIC) AccessLevel.PRIVATE else AccessLevel.PUBLIC
                    )
                }
            )

            Spacer(Modifier.height(24.dp))

            JustWooPrimaryButton(
                text = "Save task",
                onClick = { viewModel.submit(currentUserId, currentHouseId) },
                loading = state.loading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(JustWooColors.CreamSurface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, color = JustWooColors.TextSecondary)
    }
}
