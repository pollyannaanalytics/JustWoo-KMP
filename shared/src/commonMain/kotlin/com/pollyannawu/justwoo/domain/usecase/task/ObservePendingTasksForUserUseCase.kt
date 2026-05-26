package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reactive feed driving the Task Exploration ("invitations") screen — emits the
 * caller's pending tasks whenever the cache changes.
 */
class ObservePendingTasksForUserUseCase(
    private val taskRepository: TaskRepository,
    private val filterPendingTasksForUser: FilterPendingTasksForUserUseCase,
) {
    operator fun invoke(userId: Long): Flow<List<Task>> =
        taskRepository.observeTasks().map { filterPendingTasksForUser(it, userId) }
}
