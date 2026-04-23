package com.pollyannawu.justwoo.android.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Calendar / schedule view. Derived from the Figma sticky note:
 *   寫使用者story calendar劃假頁面 - 頁面長得像create task
 *   可看見自己已排程的事項
 */
class CalendarViewModel(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    data class UiState(
        val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        val loading: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val tasks: StateFlow<List<Task>> = taskRepository
        .observeTasks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun selectDate(date: LocalDate) = _uiState.update { it.copy(selectedDate = date) }

    fun tasksOnSelectedDate(all: List<Task>): List<Task> {
        val date = _uiState.value.selectedDate
        val tz = TimeZone.currentSystemDefault()
        return all.filter { it.dueTime.toLocalDateTime(tz).date == date }
    }

    fun loadRange(startIso: String, endIso: String) {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                taskRepository.getTasksByDateRange(startIso, endIso)
            } catch (t: Throwable) {
                _uiState.update { it.copy(error = t.message ?: "Couldn't load calendar.") }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }
}
