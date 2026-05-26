package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

/**
 * Snapshot variant of [ObserveProfileTasksInWindowUseCase] for iOS / SKIE.
 */
class GetProfileTasksInWindowUseCase(
    private val taskRepository: TaskRepository,
    private val filterTasksInWindow: FilterTasksInWindowUseCase,
) {
    suspend operator fun invoke(
        anchor: LocalDate,
        daysBefore: Int = ObserveProfileTasksInWindowUseCase.DEFAULT_DAYS_BEFORE,
    ): List<Task> {
        val snapshot = taskRepository.observeTasks().first()
        return filterTasksInWindow(
            tasks = snapshot,
            daysBefore = daysBefore,
            daysAfter = 0,
            anchor = anchor,
        )
    }
}
