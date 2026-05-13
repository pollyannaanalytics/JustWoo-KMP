package com.pollyannawu.justwoo.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Feeds the Home screen's task list with the "received" / "sent" / "done"
 * tabs implied by the Figma note:
 *   我接收&我發出的Task頁面 >> task overview page?
 *   完成task的呈現方式 (在首頁)
 */
class HomeViewModel(
    private val taskRepository: TaskRepository,
    private val houseRepository: HouseRepository,
) : ViewModel() {

    enum class Filter { Received, Sent, Done }

    data class UiState(
        val filter: Filter = Filter.Received,
        val refreshing: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val tasks: StateFlow<List<Task>> = taskRepository
        .observeTasks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setFilter(filter: Filter) = _uiState.update { it.copy(filter = filter) }

    fun filteredTasks(currentUserId: Long): Flow<List<Task>> =
        taskRepository.observeTasks().map { list ->
            when (_uiState.value.filter) {
                Filter.Received -> list.filter { t ->
                    t.assignees.any { it.userId == currentUserId }
                }
                Filter.Sent -> list.filter { it.ownerId == currentUserId }
                Filter.Done -> list.filter { it.taskStatus == com.pollyannawu.justwoo.core.TaskStatus.DONE }
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
