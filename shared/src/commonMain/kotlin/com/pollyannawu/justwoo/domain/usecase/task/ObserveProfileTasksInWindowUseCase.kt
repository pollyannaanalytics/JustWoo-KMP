package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * Reactive Profile feed: tasks whose `dueTime.date` is in
 * `[anchor - daysBefore, anchor]` — i.e. looking backwards from a chosen day.
 *
 * Defaults match the "look at the last week from the selected date" product spec.
 */
class ObserveProfileTasksInWindowUseCase(
    private val taskRepository: TaskRepository,
    private val filterTasksInWindow: FilterTasksInWindowUseCase,
) {
    operator fun invoke(
        anchor: LocalDate,
        daysBefore: Int = DEFAULT_DAYS_BEFORE,
    ): Flow<List<Task>> =
        taskRepository.observeTasks().map { tasks ->
            filterTasksInWindow(
                tasks = tasks,
                daysBefore = daysBefore,
                daysAfter = 0,
                anchor = anchor,
            )
        }

    companion object {
        const val DEFAULT_DAYS_BEFORE = 7
    }
}
