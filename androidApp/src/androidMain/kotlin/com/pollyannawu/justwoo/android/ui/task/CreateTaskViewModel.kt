package com.pollyannawu.justwoo.android.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

class CreateTaskViewModel(
    private val taskRepository: TaskRepository,
    private val houseRepository: HouseRepository,
) : ViewModel() {

    data class Assignee(val id: Long, val label: String)

    data class UiState(
        val title: String = "",
        val description: String = "",
        val dueTime: Instant = Clock.System.now().plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault()),
        val accessLevel: AccessLevel = AccessLevel.PUBLIC,
        val assigneeId: Long? = null, // null == "Everyone"
        val availableAssignees: List<Assignee> = emptyList(),
        val titleError: String? = null,
        val dueDateWarning: String? = null,
        val loading: Boolean = false,
        val saved: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun bind(houseId: Long) {
        viewModelScope.launch {
            houseRepository.observeHouses()
                .map { houses -> houses.firstOrNull { it.id == houseId }?.members.orEmpty() }
                .collect { members ->
                    _uiState.update { state ->
                        state.copy(
                            availableAssignees = members.map {
                                Assignee(id = it.userId, label = "Member #${it.userId}")
                            }
                        )
                    }
                }
        }
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v, titleError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onDueTimeChange(v: Instant) {
        val warning = if (v < Clock.System.now()) "You picked a past date." else null
        _uiState.update { it.copy(dueTime = v, dueDateWarning = warning) }
    }
    fun onAssigneeChange(id: Long?) = _uiState.update { it.copy(assigneeId = id) }
    fun onAccessLevelChange(level: AccessLevel) = _uiState.update { it.copy(accessLevel = level) }

    fun submit(currentUserId: Long, houseId: Long) {
        val s = _uiState.value
        if (s.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Please enter a task title.") }
            return
        }
        // "Everyone" → all house members; otherwise just the picked one.
        val assigneeIds = if (s.assigneeId == null) {
            s.availableAssignees.map { it.id }.ifEmpty { listOf(currentUserId) }
        } else {
            listOf(s.assigneeId)
        }

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
                        assigneeIds = assigneeIds,
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
