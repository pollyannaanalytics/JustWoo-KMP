package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskStatus
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reactive Home feed: tasks within `[today - daysBefore, today + daysAfter]`
 * that the user is an ACCEPTED assignee on and that aren't DONE yet.
 *
 * Window defaults to ±3 days per product spec; callers can override.
 */
class ObserveHomeTodayTasksUseCase(
    private val taskRepository: TaskRepository,
    private val filterTasksInWindow: FilterTasksInWindowUseCase,
) {
    operator fun invoke(
        userId: Long,
        daysBefore: Int = DEFAULT_DAYS_BEFORE,
        daysAfter: Int = DEFAULT_DAYS_AFTER,
    ): Flow<List<Task>> =
        taskRepository.observeTasks().map { tasks ->
            filterTasksInWindow(tasks = tasks, daysBefore = daysBefore, daysAfter = daysAfter)
                .filter { it.matchesHomeFeed(userId) }
        }

    companion object {
        const val DEFAULT_DAYS_BEFORE = 3
        const val DEFAULT_DAYS_AFTER = 3
    }
}

internal fun Task.matchesHomeFeed(userId: Long): Boolean =
    taskStatus != TaskStatus.DONE &&
        assignees.any { it.userId == userId && it.status == AssignStatus.ACCEPTED }
