package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.coroutines.flow.first

/**
 * Snapshot variant of [ObserveHomeTodayTasksUseCase] — intended for iOS via SKIE,
 * where a one-shot suspend call maps cleanly onto Swift's async/await.
 */
class GetHomeTodayTasksUseCase(
    private val taskRepository: TaskRepository,
    private val filterTasksInWindow: FilterTasksInWindowUseCase,
) {
    suspend operator fun invoke(
        userId: Long,
        daysBefore: Int = ObserveHomeTodayTasksUseCase.DEFAULT_DAYS_BEFORE,
        daysAfter: Int = ObserveHomeTodayTasksUseCase.DEFAULT_DAYS_AFTER,
    ): List<Task> {
        val snapshot = taskRepository.observeTasks().first()
        return filterTasksInWindow(tasks = snapshot, daysBefore = daysBefore, daysAfter = daysAfter)
            .filter { it.matchesHomeFeed(userId) }
    }
}
