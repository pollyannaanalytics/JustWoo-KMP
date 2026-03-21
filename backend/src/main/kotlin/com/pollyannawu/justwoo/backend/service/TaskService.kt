package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.ProfileRepository
import com.pollyannawu.justwoo.backend.repositories.TaskRepository
import com.pollyannawu.justwoo.backend.utils.dataresult.TaskDataResult
import com.pollyannawu.justwoo.backend.utils.dataresult.TaskUserType
import com.pollyannawu.justwoo.backend.utils.mapper.toDomain
import com.pollyannawu.justwoo.backend.utils.mapper.toResponse
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.dto.TaskResponse
import com.pollyannawu.justwoo.core.TaskStatus

interface TaskService {
    suspend fun getTaskDetails(
        houseId: Long,
        userId: Long,
        taskStatus: TaskStatus? = null
    ): TaskDataResult<List<TaskResponse>>

    suspend fun getTasksByOwnerId(
        houseId: Long,
        userId: Long,
        ownerId: Long,
        taskStatus: TaskStatus? = null
    ): TaskDataResult<List<TaskResponse>>

    suspend fun getTasksByAssigneeId(
        houseId: Long,
        userId: Long,
        assigneeId: Long,
        taskStatus: TaskStatus? = null
    ): TaskDataResult<List<TaskResponse>>

    suspend fun createTask(
        houseId: Long,
        userId: Long,
        taskRequest: CreateTaskRequest
    ): TaskDataResult<TaskResponse>

    suspend fun updateTaskContent(
        houseId: Long,
        ownerId: Long,
        task: Task
    ): TaskDataResult<TaskResponse>

    suspend fun updateTaskAssignStatus(
        houseId: Long,
        taskId: Long,
        assignee: TaskAssignee
    ): TaskDataResult<TaskResponse>

    suspend fun updateTaskStatus(
        houseId: Long,
        assigneeId: Long,
        taskId: Long,
        taskStatus: TaskStatus
    ): TaskDataResult<TaskResponse>
}

internal class DefaultTaskService(
    private val houseRepo: HouseRepository,
    private val taskRepo: TaskRepository,
    private val profileRepo: ProfileRepository
) : TaskService {
    private suspend fun getTaskDomains(
        houseId: Long,
        taskStatus: TaskStatus?
    ): List<Task> {
        return taskRepo.getTasks(houseId, taskStatus)
    }

    private suspend fun mergeTaskAssigneeResponses(tasks: List<Task>): List<TaskResponse> {
        val assigneesId = tasks.flatMap { it.assignees }.map { it.userId }.distinct()
        val profilesMap = profileRepo.getProfiles(assigneesId).associateBy { it.id }

        return tasks.map {
            val taskAssigneeResponses = it.assignees.map { assignee ->
                val profile = profilesMap[assignee.userId]
                profile?.let { assignee.toResponse(it) }
            }.filterNotNull()

            it.toResponse(taskAssigneeResponses)
        }
    }

    private suspend fun mergeTaskAssigneeResponse(task: Task): TaskResponse {
        return mergeTaskAssigneeResponses(listOf(task)).first()
    }

    override suspend fun getTaskDetails(
        houseId: Long,
        userId: Long,
        taskStatus: TaskStatus?
    ): TaskDataResult<List<TaskResponse>> {
        if (!houseRepo.isMember(userId, houseId)) return TaskDataResult.Error.UserNotAllowed(
            userId,
            TaskUserType.REQUEST
        )
        val tasks = getTaskDomains(houseId, taskStatus)
        val tasksResponse = mergeTaskAssigneeResponses(tasks)
        return TaskDataResult.Success(tasksResponse)
    }


    override suspend fun getTasksByOwnerId(
        houseId: Long,
        userId: Long,
        ownerId: Long,
        taskStatus: TaskStatus?
    ): TaskDataResult<List<TaskResponse>> {
        if (!houseRepo.isMember(ownerId, houseId)) return TaskDataResult.Error.UserNotAllowed(
            ownerId,
            TaskUserType.OWNER
        )
        val tasks = taskRepo.getTasksByOwnerId(houseId, ownerId, taskStatus)

        val tasksResponse = mergeTaskAssigneeResponses(tasks)
        return TaskDataResult.Success(tasksResponse)
    }

    override suspend fun getTasksByAssigneeId(
        houseId: Long,
        userId: Long,
        assigneeId: Long,
        taskStatus: TaskStatus?
    ): TaskDataResult<List<TaskResponse>> {
        if (!houseRepo.isMember(assigneeId, houseId)) return TaskDataResult.Error.UserNotAllowed(
            assigneeId,
            TaskUserType.ASSIGNEE
        )

        val tasks = taskRepo.getTasksByAssigneeId(houseId, assigneeId, taskStatus)

        val tasksResponse = mergeTaskAssigneeResponses(tasks)
        return TaskDataResult.Success(tasksResponse)
    }

    override suspend fun createTask(
        houseId: Long,
        userId: Long,
        taskRequest: CreateTaskRequest
    ): TaskDataResult<TaskResponse> {
        if (!houseRepo.isMember(userId, houseId)) return TaskDataResult.Error.UserNotAllowed(
            userId,
            TaskUserType.REQUEST
        )
        try {
            val newTask = taskRequest.toDomain()
            val result = taskRepo.createTask(newTask)
            return TaskDataResult.Success(mergeTaskAssigneeResponse(result))
        } catch (e: Exception) {
            return TaskDataResult.Error.DatabaseError(
                e.message ?: "Unknown Exception when createTask"
            )
        }
    }

    override suspend fun updateTaskContent(
        houseId: Long,
        ownerId: Long,
        task: Task
    ): TaskDataResult<TaskResponse> {
        if (!houseRepo.isMember(ownerId, houseId)) return TaskDataResult.Error.UserNotAllowed(
            ownerId,
            TaskUserType.OWNER
        )
        return try {
            TaskDataResult.Success(mergeTaskAssigneeResponse(task))
        } catch (e: Exception) {
            TaskDataResult.Error.DatabaseError(
                e.message ?: "Unknown Exception when updateTaskContent"
            )
        }
    }

    override suspend fun updateTaskAssignStatus(
        houseId: Long,
        taskId: Long,
        assignee: TaskAssignee
    ): TaskDataResult<TaskResponse> {
        if (!houseRepo.isMember(
                assignee.userId,
                houseId
            )
        ) return TaskDataResult.Error.UserNotAllowed(
            assignee.userId,
            TaskUserType.ASSIGNEE
        )
        try {
            if (assignee.status == AssignStatus.ACCEPTED) {
                taskRepo.updateTaskAssignStatus(taskId, assignee.userId, assignee.status)
                val result = taskRepo.updateTaskExecutor(assignee.userId, taskId)
                return TaskDataResult.Success(mergeTaskAssigneeResponse(result))
            } else {
                return TaskDataResult.Error.AssigneeStatusError
            }
        } catch (e: Exception) {
            return TaskDataResult.Error.DatabaseError(
                e.message ?: "Unknown Exception when updateTaskAssignStatus"
            )
        }
    }

    override suspend fun updateTaskStatus(
        houseId: Long,
        assigneeId: Long,
        taskId: Long,
        taskStatus: TaskStatus
    ): TaskDataResult<TaskResponse> {
        if (!houseRepo.isMember(assigneeId, houseId) || !taskRepo.isTaskExecutor(
                assigneeId,
                taskId
            )
        ) return TaskDataResult.Error.UserNotAllowed(
            assigneeId,
            TaskUserType.ASSIGNEE
        )

        try {
            val result = taskRepo.updateTaskStatus(taskId, taskStatus)
            return TaskDataResult.Success(mergeTaskAssigneeResponse(result))
        } catch (e: Exception) {
            return TaskDataResult.Error.DatabaseError(
                e.message ?: "Unknown Exception when updateTaskAssignStatus"
            )
        }
    }


}