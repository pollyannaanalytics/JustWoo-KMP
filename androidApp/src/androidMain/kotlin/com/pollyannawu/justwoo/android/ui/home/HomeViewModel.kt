package com.pollyannawu.justwoo.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskStatus
import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.TaskRepository
import com.pollyannawu.justwoo.domain.usecase.task.ObserveHomeTodayTasksUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val houseRepository: HouseRepository,
    private val observeHomeTodayTasks: ObserveHomeTodayTasksUseCase,
) : ViewModel() {

    data class UiState(
        val refreshing: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Tasks for the current user within ±3 days of today (defaults from the use case). */
    fun todayTasks(currentUserId: Long): Flow<List<Task>> =
        observeHomeTodayTasks(userId = currentUserId)

    /** Count of tasks involving the user within the current calendar month. */
    fun monthlyCount(currentUserId: Long): Flow<Int> =
        taskRepository.observeTasks().map { tasks ->
            val zone = TimeZone.currentSystemDefault()
            val now = Clock.System.now().toLocalDateTime(zone)
            tasks.count { t ->
                val due = t.dueTime.toLocalDateTime(zone)
                val involves = t.assignees.any { it.userId == currentUserId } && t.taskStatus == TaskStatus.DONE
                involves && due.year == now.year && due.monthNumber == now.monthNumber
            }
        }

    fun refresh(houseId: Long) {
        _uiState.update { it.copy(refreshing = true, error = null) }
        viewModelScope.launch {
            try {
                houseRepository.refreshHouses()
                taskRepository.refreshTasks(houseId = houseId, page = 0)
            } catch (t: Throwable) {
                _uiState.update { it.copy(error = t.message ?: "Refresh failed.") }
            } finally {
                _uiState.update { it.copy(refreshing = false) }
            }
        }
    }
}
