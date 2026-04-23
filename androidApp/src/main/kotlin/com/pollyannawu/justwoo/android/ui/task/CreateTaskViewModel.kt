package com.pollyannawu.justwoo.android.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

/**
 * Drives the "Create task" screen. Error states mirror the sticky notes
 * from the Figma file:
 *   - 無填寫task title               (title missing)
 *   - 沒有指定對象 (default)            (no assignee -> default to self)
 *   - 日曆選擇過去日期 (給予提醒)     (past date -> warn)
 */
class CreateTaskViewModel(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    data class UiState(
        val title: String = "",
        val description: String = "",
        val dueTime: Instant = Clock.System.now().plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault()),
        val assigneeIds: List<Long> = emptyList(),
        val accessLevel: AccessLevel = AccessLevel.PUBLIC,
        val titleError: String? = null,
        val dueDateWarning: String? = null,
        val loading: Boolean = false,
        val saved: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, titleError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onDueTimeChange(v: Instant) {
        val warning = if (v < Clock.System.now()) "You picked a past date." else null
        _uiState.update { it.copy(dueTime = v, dueDateWarning = warning) }
    }
    fun onAssigneesChange(ids: List<Long>) = _uiState.update { it.copy(assigneeIds = ids) }
    fun onAccessLevelChange(level: AccessLevel) = _uiState.update { it.copy(accessLevel = level) }

    fun submit(currentUserId: Long, houseId: Long) {
        val s = _uiState.value
        if (s.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Please enter a task title.") }
            return
        }
        // Default to the current user if no assignee picked — matches Figma note.
        val assignees = s.assigneeIds.ifEmpty { listOf(currentUserId) }

        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            try {
                taskRepository.createTask(
                    CreateTaskRequest(
                        title = s.title.trim(),
                        ownerId = currentUserId,
                        description = s.description.takeIf { it.isNotBlank() },
                        houseId = houseId,
                        accessLevel = s.accessLevel,
                        assigneeIds = assignees,
                        dueTime = s.dueTime,
                    )
                )
                _uiState.update { it.copy(loading = false, saved = true) }
            } catch (t: Throwable) {
                _uiState.update { it.copy(loading = false, titleError = t.message ?: "Could not save task.") }
            }
        }
    }

    fun consumeSaved() = _uiState.update { it.copy(saved = false) }
}
