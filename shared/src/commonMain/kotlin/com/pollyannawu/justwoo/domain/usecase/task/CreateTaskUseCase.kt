package com.pollyannawu.justwoo.domain.usecase.task

import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.data.TaskRepository
import kotlinx.datetime.Instant

/** Result of an attempt to create a task. */
sealed class CreateTaskOutcome {
    data object Success : CreateTaskOutcome()

    sealed class Failure : CreateTaskOutcome() {
        /** Title was blank / whitespace-only — caught locally before any network call. */
        data object BlankTitle : Failure()

        /** Network / server / unknown error from the data layer. */
        data class Unknown(val message: String?) : Failure()
    }
}

/**
 * Validates user input, expands the "Everyone" assignee into the full member
 * roster (falling back to the owner alone if a house has no other members),
 * builds the [CreateTaskRequest] and forwards it to [TaskRepository].
 *
 * Lives in `commonMain` so Android + iOS share the exact same submission rules.
 */
class CreateTaskUseCase(
    private val taskRepository: TaskRepository,
) {
    data class Input(
        val title: String,
        val description: String?,
        val ownerId: Long,
        val houseId: Long,
        val accessLevel: AccessLevel,
        /** `null` is the "Everyone" choice; otherwise pick exactly one member. */
        val assigneeId: Long?,
        /** Member ids the user can pick from — used to expand "Everyone". */
        val availableMemberIds: List<Long>,
        val dueTime: Instant,
    )

    suspend operator fun invoke(input: Input): CreateTaskOutcome {
        val title = input.title.trim()
        if (title.isEmpty()) return CreateTaskOutcome.Failure.BlankTitle

        val assigneeIds = if (input.assigneeId == null) {
            input.availableMemberIds.ifEmpty { listOf(input.ownerId) }
        } else {
            listOf(input.assigneeId)
        }

        return try {
            taskRepository.createTask(
                CreateTaskRequest(
                    title = title,
                    ownerId = input.ownerId,
                    description = input.description?.takeIf { it.isNotBlank() },
                    houseId = input.houseId,
                    accessLevel = input.accessLevel,
                    assigneeIds = assigneeIds,
                    dueTime = input.dueTime,
                )
            )
            CreateTaskOutcome.Success
        } catch (t: Throwable) {
            CreateTaskOutcome.Failure.Unknown(t.message)
        }
    }
}
