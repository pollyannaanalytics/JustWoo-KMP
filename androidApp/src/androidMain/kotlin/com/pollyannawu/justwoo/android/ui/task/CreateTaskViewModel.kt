package com.pollyannawu.justwoo.android.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.domain.usecase.house.ObserveHouseMembersUseCase
import com.pollyannawu.justwoo.domain.usecase.task.CreateTaskOutcome
import com.pollyannawu.justwoo.domain.usecase.task.CreateTaskUseCase
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

class CreateTaskViewModel(
    private val observeHouseMembers: ObserveHouseMembersUseCase,
    private val createTask: CreateTaskUseCase,
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
            observeHouseMembers(houseId).collect { members ->
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
        _uiState.update { it.copy(loading = true, titleError = null) }
        viewModelScope.launch {
            val outcome = createTask(
                CreateTaskUseCase.Input(
                    title = s.title,
                    description = s.description,
                    ownerId = currentUserId,
                    houseId = houseId,
                    accessLevel = s.accessLevel,
                    assigneeId = s.assigneeId,
                    availableMemberIds = s.availableAssignees.map { it.id },
                    dueTime = s.dueTime,
                )
            )
            when (outcome) {
                CreateTaskOutcome.Success ->
                    _uiState.update { it.copy(loading = false, saved = true) }
                CreateTaskOutcome.Failure.BlankTitle ->
                    _uiState.update { it.copy(loading = false, titleError = "Please enter a task title.") }
                is CreateTaskOutcome.Failure.Unknown ->
                    _uiState.update {
                        it.copy(loading = false, titleError = outcome.message ?: "Could not save task.")
                    }
            }
        }
    }

    fun consumeSaved() = _uiState.update { it.copy(saved = false) }
}
