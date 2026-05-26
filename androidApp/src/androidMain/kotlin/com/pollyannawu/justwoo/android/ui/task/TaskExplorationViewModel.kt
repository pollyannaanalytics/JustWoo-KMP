package com.pollyannawu.justwoo.android.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.domain.usecase.task.ObservePendingTasksForUserUseCase
import com.pollyannawu.justwoo.domain.usecase.task.SubmitTaskDecisionUseCase
import com.pollyannawu.justwoo.domain.usecase.task.TaskDecisionOutcome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class TaskExplorationViewModel(
    private val observePendingTasksForUser: ObservePendingTasksForUserUseCase,
    private val submitTaskDecision: SubmitTaskDecisionUseCase,
) : ViewModel() {

    data class UiState(
        val tasks: List<Task> = emptyList(),
        val currentIndex: Int = 0,
        val submitting: Boolean = false,
        val error: String? = null,
    ) {
        val total: Int get() = tasks.size
        val current: Task? get() = tasks.getOrNull(currentIndex)
        val hasMore: Boolean get() = currentIndex < tasks.size
    }

    private val _currentIndex = MutableStateFlow(0)
    private val _submitting = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _userId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState> = combine(
        _userId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else observePendingTasksForUser(id)
        },
        _currentIndex,
        _submitting,
        _error,
    ) { tasks, idx, submitting, err ->
        UiState(
            tasks = tasks,
            currentIndex = idx.coerceAtMost(tasks.size),
            submitting = submitting,
            error = err,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    fun bind(userId: Long) {
        _userId.value = userId
        _currentIndex.value = 0
    }

    fun accept(houseId: Long, userId: Long, taskId: Long) =
        submitDecision(houseId, userId, taskId, AssignStatus.ACCEPTED)

    fun decline(houseId: Long, userId: Long, taskId: Long) =
        submitDecision(houseId, userId, taskId, AssignStatus.REJECTED)

    private fun submitDecision(
        houseId: Long,
        userId: Long,
        taskId: Long,
        decision: AssignStatus,
    ) {
        _submitting.value = true
        _error.value = null
        viewModelScope.launch {
            when (val outcome = submitTaskDecision(
                houseId = houseId,
                taskId = taskId,
                userId = userId,
                decision = decision,
            )) {
                TaskDecisionOutcome.Success ->
                    _currentIndex.update { it + 1 }
                TaskDecisionOutcome.Failure.InvalidStatus ->
                    _error.value = "Invalid decision."
                is TaskDecisionOutcome.Failure.Unknown ->
                    _error.value = outcome.message ?: "Could not update task."
            }
            _submitting.value = false
        }
    }
}
