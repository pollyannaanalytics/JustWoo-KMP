package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.first

/**
 * Snapshot variant of [ObservePendingTasksForUserUseCase] — for iOS / SKIE.
 */
class GetPendingTasksForUserUseCase(
    private val taskRepository: TaskRepository,
    private val filterPendingTasksForUser: FilterPendingTasksForUserUseCase,
) {
    suspend operator fun invoke(userId: Long): List<Task> =
        filterPendingTasksForUser(taskRepository.observeTasks().first(), userId)
}
