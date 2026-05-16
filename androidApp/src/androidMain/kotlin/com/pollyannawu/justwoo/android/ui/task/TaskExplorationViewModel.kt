package com.pollyannawu.justwoo.android.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the "Task Exploration" swipe deck: the user sees one card per pending
 * task assigned to them, and swipes right to accept / left to decline.
 *
 * UNASSIGNED + PENDING_APPROVAL count as "pending" — anything the user has yet
 * to act on.
 */
class TaskExplorationViewModel(
    private val taskRepository: TaskRepository,
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
    private val _userId = MutableStateFlow(0L)

    val uiState: StateFlow<UiState> = combine(
        taskRepository.observeTasks().map { it.filterPendingFor(_userId.value) },
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
        status: AssignStatus,
    ) {
        _submitting.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                taskRepository.updateTaskAssignStatus(
                    houseId = houseId,
                    taskId = taskId,
                    assignee = TaskAssignee(userId = userId, status = status),
                )
                _currentIndex.update { it + 1 }
            } catch (t: Throwable) {
                _error.value = t.message ?: "Could not update task."
            } finally {
                _submitting.value = false
            }
        }
    }

    private fun List<Task>.filterPendingFor(userId: Long): List<Task> =
        filter { task ->
            task.assignees.any { a ->
                a.userId == userId &&
                    (a.status == AssignStatus.UNASSIGNED || a.status == AssignStatus.PENDING_APPROVAL)
            }
        }.sortedBy { it.dueTime }
}
