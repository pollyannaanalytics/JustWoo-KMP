package com.pollyannawu.justwoo.android.ui.task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.nav.LocalAppActions
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.ui.nav.tasks.TaskEditComponent
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooShapes
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.design.DesignTokens
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    currentUserId: Long,
    currentHouseId: Long,
    component: TaskEditComponent,
    viewModel: CreateTaskViewModel = koinViewModel(),
) {
    LaunchedEffect(currentHouseId) { viewModel.bind(currentHouseId) }
    val state by viewModel.uiState.collectAsState()
    val appActions = LocalAppActions.current

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            component.onSave()
        }
    }

    CreateTaskContent(
        state = state,
        onClose = component::onCancel,
        onOpenProfile = appActions.onProfileClick,
        onTitleChange = viewModel::onTitleChange,
        onAssigneeChange = viewModel::onAssigneeChange,
        onAccessLevelChange = viewModel::onAccessLevelChange,
        onDueTimeChange = viewModel::onDueTimeChange,
        onSubmit = { viewModel.submit(currentUserId, currentHouseId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTaskContent(
    state: CreateTaskViewModel.UiState,
    onClose: () -> Unit,
    onOpenProfile: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAssigneeChange: (Long?) -> Unit,
    onAccessLevelChange: (AccessLevel) -> Unit,
    onDueTimeChange: (Instant) -> Unit,
    onSubmit: () -> Unit,
) {
    var datePickerOpen by remember { mutableStateOf(false) }

    if (datePickerOpen) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.dueTime.toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { datePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onDueTimeChange(Instant.fromEpochMilliseconds(millis))
                    }
                    datePickerOpen = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { datePickerOpen = false }) { Text("Cancel") }
            },
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(containerColor = JustWooColors.Cream) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = JustWooSpacing.XLarge),
            verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Large),
        ) {
            CreateTaskTopBar(
                onClose = onClose,
                onOpenProfile = onOpenProfile,
            )

            JustWooTextField(
                value = state.title,
                onValueChange = onTitleChange,
                placeholder = "task title",
                isError = state.titleError != null,
                errorMessage = state.titleError,
                modifier = Modifier.fillMaxWidth()
            )

            AssignToRow(
                assignees = state.availableAssignees,
                selectedId = state.assigneeId,
                onSelect = onAssigneeChange,
            )

            PublicToggleRow(
                accessLevel = state.accessLevel,
                onToggle = onAccessLevelChange,
            )

            Text(
                text = "Select due time",
                color = JustWooColors.TextPrimary,
                fontWeight = JustWooFontWeight.Bold,
                fontSize = DesignTokens.FontSize.TitleLarge.sp,
            )

            DueTimeCard(
                dueTime = state.dueTime,
                warning = state.dueDateWarning,
                onClick = { datePickerOpen = true },
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = JustWooSpacing.XLarge),
                horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.Default),
            ) {
                FooterButton(
                    text = "Cancel",
                    background = JustWooColors.Outline,
                    contentColor = JustWooColors.TextPrimary,
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                )
                FooterButton(
                    text = if (state.loading) "Saving…" else "Done",
                    background = JustWooColors.Primary,
                    contentColor = JustWooColors.OnPrimary,
                    enabled = !state.loading,
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CreateTaskTopBar(onClose: () -> Unit, onOpenProfile: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = JustWooSpacing.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = JustWooColors.TextPrimary,
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "Create a task",
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = DesignTokens.FontSize.Title.sp,
        )
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(JustWooColors.TextPrimary)
                .clickable { onOpenProfile() },
        )
    }
}

@Composable
private fun AssignToRow(
    assignees: List<CreateTaskViewModel.Assignee>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = if (selectedId == null) "Everyone"
    else assignees.firstOrNull { it.id == selectedId }?.label ?: "Member #$selectedId"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.Medium),
    ) {
        Text(
            text = "Assign to",
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = DesignTokens.FontSize.Title.sp,
        )

        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(JustWooShapes.Small)
                    .border(1.dp, JustWooColors.Outline, JustWooShapes.Small)
                    .background(JustWooColors.CreamSurface)
                    .clickable { expanded = true }
                    .padding(horizontal = JustWooSpacing.Medium, vertical = JustWooSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectedId == null) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = JustWooColors.UrgencyGreen,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(JustWooSpacing.Small))
                }
                Text(
                    text = selectedLabel,
                    color = JustWooColors.TextPrimary,
                    fontSize = DesignTokens.FontSize.BodyLarge.sp,
                    fontWeight = JustWooFontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Open",
                    tint = JustWooColors.TextSecondary,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(JustWooColors.CreamSurface),
            ) {
                DropdownMenuItem(
                    text = { Text("Everyone") },
                    onClick = { onSelect(null); expanded = false },
                )
                assignees.forEach { a ->
                    DropdownMenuItem(
                        text = { Text(a.label) },
                        onClick = { onSelect(a.id); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicToggleRow(
    accessLevel: AccessLevel,
    onToggle: (AccessLevel) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(JustWooSpacing.Default),
    ) {
        Text(
            text = "Public",
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = DesignTokens.FontSize.Title.sp,
        )
        TogglePill(
            selected = accessLevel == AccessLevel.PRIVATE,
            selectedColor = Color.White,
            onClick = { onToggle(AccessLevel.PRIVATE) },
        )
        TogglePill(
            selected = accessLevel == AccessLevel.PUBLIC,
            selectedColor = JustWooColors.Primary,
            onClick = { onToggle(AccessLevel.PUBLIC) },
        )
    }
}

@Composable
private fun TogglePill(
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) selectedColor else JustWooColors.Outline)
            .border(1.dp, JustWooColors.Outline, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = if (selected) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(2.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, JustWooColors.Outline, CircleShape),
        )
    }
}

@Composable
private fun DueTimeCard(
    dueTime: Instant,
    warning: String?,
    onClick: () -> Unit,
) {
    val local = dueTime.toLocalDateTime(TimeZone.currentSystemDefault())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(JustWooShapes.Large)
            .background(JustWooColors.CreamElevated)
            .border(1.dp, JustWooColors.Outline, JustWooShapes.Large)
            .clickable { onClick() }
            .padding(JustWooSpacing.Large),
        verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Small),
    ) {
        Text(
            text = monthName(local.monthNumber),
            color = JustWooColors.TextPrimary,
            fontWeight = JustWooFontWeight.Bold,
            fontSize = DesignTokens.FontSize.TitleLarge.sp,
            modifier = Modifier.wrapContentSize(Alignment.Center).fillMaxWidth(),
        )
        Text(
            text = "${local.year}/${local.monthNumber}/${local.dayOfMonth} " +
                "%02d:%02d".format(local.hour, local.minute),
            color = JustWooColors.TextSecondary,
            fontSize = DesignTokens.FontSize.Body.sp,
            modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.Center),
        )
        if (warning != null) {
            Text(
                text = "*$warning",
                color = JustWooColors.Error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun FooterButton(
    text: String,
    background: Color,
    contentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(JustWooShapes.Medium)
            .background(if (enabled) background else background.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = contentColor,
            fontWeight = JustWooFontWeight.ExtraBold,
            fontSize = DesignTokens.FontSize.Label.sp,
        )
    }
}

private fun monthName(monthNumber: Int): String = when (monthNumber) {
    1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
    5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
    9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
    else -> ""
}
