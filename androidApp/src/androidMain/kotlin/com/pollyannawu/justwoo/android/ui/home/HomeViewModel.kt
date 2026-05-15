package com.pollyannawu.justwoo.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskStatus
import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.TaskRepository
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

/**
 * Drives the Home screen — the cards on the Figma "Homepage v4_re_Today" page.
 */
class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val houseRepository: HouseRepository,
) : ViewModel() {

    data class UiState(
        val refreshing: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Tasks assigned to (and accepted by) the current user that are due today. */
    fun todayTasks(currentUserId: Long): Flow<List<Task>> =
        taskRepository.observeTasks().map { tasks ->
            val zone = TimeZone.currentSystemDefault()
            val today = Clock.System.now().toLocalDateTime(zone).date
            tasks
                .filter { t ->
                    t.taskStatus != TaskStatus.DONE &&
                        t.assignees.any { a ->
                            a.userId == currentUserId && a.status == AssignStatus.ACCEPTED
                        } &&
                        t.dueTime.toLocalDateTime(zone).date == today
                }
                .sortedBy { it.dueTime }
        }

    /** Count of tasks involving the user within the current calendar month. */
    fun monthlyCount(currentUserId: Long): Flow<Int> =
        taskRepository.observeTasks().map { tasks ->
            val zone = TimeZone.currentSystemDefault()
            val now = Clock.System.now().toLocalDateTime(zone)
            tasks.count { t ->
                val due = t.dueTime.toLocalDateTime(zone)
                val involves = t.ownerId == currentUserId ||
                    t.assignees.any { it.userId == currentUserId }
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
