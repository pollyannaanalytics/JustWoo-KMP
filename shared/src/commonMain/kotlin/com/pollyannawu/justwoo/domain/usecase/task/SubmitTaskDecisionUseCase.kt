package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.data.TaskRepository

/** Accept / decline outcome surfaced to the ViewModel. */
sealed class TaskDecisionOutcome {
    data object Success : TaskDecisionOutcome()

    sealed class Failure : TaskDecisionOutcome() {
        /** AssignStatus passed in wasn't ACCEPTED or REJECTED. */
        data object InvalidStatus : Failure()

        /** Network / server / unknown — caller can surface `message` in UI. */
        data class Unknown(val message: String?) : Failure()
    }
}

/**
 * Sets the caller's assignment status for a task (the swipe accept / decline
 * action on Task Exploration). Wraps the data-layer exception into a
 * domain outcome so the ViewModel doesn't have to do try/catch gymnastics.
 */
class SubmitTaskDecisionUseCase(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(
        houseId: Long,
        taskId: Long,
        userId: Long,
        decision: AssignStatus,
    ): TaskDecisionOutcome {
        if (decision != AssignStatus.ACCEPTED && decision != AssignStatus.REJECTED) {
            return TaskDecisionOutcome.Failure.InvalidStatus
        }
        return try {
            taskRepository.updateTaskAssignStatus(
                houseId = houseId,
                taskId = taskId,
                assignee = TaskAssignee(userId = userId, status = decision),
            )
            TaskDecisionOutcome.Success
        } catch (t: Throwable) {
            TaskDecisionOutcome.Failure.Unknown(t.message)
        }
    }
}
