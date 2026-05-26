package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task

/**
 * Pure filter: tasks where the given user is still owed a decision —
 * an assignee entry exists with status `UNASSIGNED` or `PENDING_APPROVAL`.
 * Sorted by `dueTime` ascending so the soonest decisions surface first.
 */
class FilterPendingTasksForUserUseCase {
    operator fun invoke(tasks: List<Task>, userId: Long): List<Task> =
        tasks
            .filter { task ->
                task.assignees.any { a ->
                    a.userId == userId &&
                        (a.status == AssignStatus.UNASSIGNED || a.status == AssignStatus.PENDING_APPROVAL)
                }
            }
            .sortedBy { it.dueTime }
}
