package com.pollyannawu.justwoo.android.ui.task

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pollyannawu.justwoo.android.ui.common.JustWooTextField
import com.pollyannawu.justwoo.android.ui.common.componentViewModelStoreOwner
import com.pollyannawu.justwoo.android.ui.nav.LocalAppActions
import com.pollyannawu.justwoo.android.ui.task.components.AssignToRow
import com.pollyannawu.justwoo.android.ui.task.components.CreateTaskFooterButtons
import com.pollyannawu.justwoo.android.ui.task.components.CreateTaskTopBar
import com.pollyannawu.justwoo.android.ui.task.components.PreviewSamples
import com.pollyannawu.justwoo.android.ui.task.components.PublicSwitchRow
import com.pollyannawu.justwoo.android.ui.task.components.WheelTimePicker
import com.pollyannawu.justwoo.android.ui.theme.JustWooColors
import com.pollyannawu.justwoo.android.ui.theme.JustWooFontWeight
import com.pollyannawu.justwoo.android.ui.theme.JustWooSpacing
import com.pollyannawu.justwoo.android.ui.theme.JustWooTheme
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.design.DesignTokens
import com.pollyannawu.justwoo.ui.nav.tasks.TaskEditComponent
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    currentUserId: Long,
    currentHouseId: Long,
    component: TaskEditComponent,
    viewModel: CreateTaskViewModel = koinViewModel(viewModelStoreOwner = componentViewModelStoreOwner(component)),
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
        onSubmit = { viewModel.submit(currentUserId, currentHouseId) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateTaskContent(
    state: CreateTaskViewModel.UiState,
    onClose: () -> Unit,
    onOpenProfile: () -> Unit,
    onTitleChange: (String) -> Unit,
    onAssigneeChange: (Long?) -> Unit,
    onAccessLevelChange: (AccessLevel) -> Unit,
    onDueTimeChange: (Instant) -> Unit,
    onSubmit: () -> Unit,
) {
    val tz = TimeZone.currentSystemDefault()
    val dueLocal = remember(state.dueTime) { state.dueTime.toLocalDateTime(tz) }

    val initialDateMillis = remember(state.dueTime) {
        LocalDateTime(dueLocal.year, dueLocal.month, dueLocal.dayOfMonth, 0, 0, 0)
            .toInstant(TimeZone.UTC).toEpochMilliseconds()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    var selectedHour by remember { mutableIntStateOf(dueLocal.hour) }
    var selectedMinute by remember { mutableIntStateOf(dueLocal.minute) }

    fun emitCombined(dateMillis: Long, hour: Int, minute: Int) {
        val d = Instant.fromEpochMilliseconds(dateMillis).toLocalDateTime(TimeZone.UTC)
        onDueTimeChange(LocalDateTime(d.year, d.month, d.dayOfMonth, hour, minute, 0).toInstant(tz))
    }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { emitCombined(it, selectedHour, selectedMinute) }
    }

    Scaffold(
        containerColor = JustWooColors.Cream,
        bottomBar = {
            CreateTaskFooterButtons(
                isSaving = state.loading,
                onCancel = onClose,
                onDone = onSubmit,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = JustWooSpacing.XLarge),
            verticalArrangement = Arrangement.spacedBy(JustWooSpacing.Large),
        ) {
            CreateTaskTopBar(onClose = onClose, onOpenProfile = onOpenProfile)

            JustWooTextField(
                value = state.title,
                onValueChange = onTitleChange,
                placeholder = "task title",
                isError = state.titleError != null,
                errorMessage = state.titleError,
                modifier = Modifier.fillMaxWidth(),
            )

            AssignToRow(
                assignees = state.availableAssignees,
                selectedId = state.assigneeId,
                onSelect = onAssigneeChange,
            )

            PublicSwitchRow(
                accessLevel = state.accessLevel,
                onToggle = onAccessLevelChange,
            )

            SectionLabel("Due Date")

            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                headline = null,
                title = null,
            )

            SectionLabel("Due Time")

            WheelTimePicker(
                hour = selectedHour,
                minute = selectedMinute,
                onHourChange = { hour ->
                    selectedHour = hour
                    datePickerState.selectedDateMillis?.let { emitCombined(it, hour, selectedMinute) }
                },
                onMinuteChange = { minute ->
                    selectedMinute = minute
                    datePickerState.selectedDateMillis?.let { emitCombined(it, selectedHour, minute) }
                },
            )

            Spacer(Modifier.height(JustWooSpacing.Large))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = JustWooColors.TextPrimary,
        fontWeight = JustWooFontWeight.Bold,
        fontSize = DesignTokens.FontSize.TitleLarge.sp,
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 1100)
@Composable
private fun CreateTaskContentLightPreview() {
    JustWooTheme {
        CreateTaskContent(
            state = PreviewSamples.uiState,
            onClose = {}, onOpenProfile = {},
            onTitleChange = {}, onAssigneeChange = {},
            onAccessLevelChange = {}, onDueTimeChange = {}, onSubmit = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Saving", showBackground = true, backgroundColor = 0xFFF5F1E7, heightDp = 1100)
@Composable
private fun CreateTaskContentSavingPreview() {
    JustWooTheme {
        CreateTaskContent(
            state = PreviewSamples.uiState.copy(loading = true),
            onClose = {}, onOpenProfile = {},
            onTitleChange = {}, onAssigneeChange = {},
            onAccessLevelChange = {}, onDueTimeChange = {}, onSubmit = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, heightDp = 1100)
@Composable
private fun CreateTaskContentDarkPreview() {
    JustWooTheme {
        CreateTaskContent(
            state = PreviewSamples.uiState,
            onClose = {}, onOpenProfile = {},
            onTitleChange = {}, onAssigneeChange = {},
            onAccessLevelChange = {}, onDueTimeChange = {}, onSubmit = {},
        )
    }
}
