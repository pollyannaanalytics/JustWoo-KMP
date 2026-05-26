package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.Flow

/**
 * Thin observer over the full cached task list — for callers that genuinely
 * need every task (e.g. calendar grid dot markers). Prefer the more specific
 * window / feed use cases for anything driven by business rules.
 */
class ObserveAllTasksUseCase(
    private val taskRepository: TaskRepository,
) {
    operator fun invoke(): Flow<List<Task>> = taskRepository.observeTasks()
}
