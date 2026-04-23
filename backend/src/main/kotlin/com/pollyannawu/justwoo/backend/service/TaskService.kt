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
import com.pollyannawu.justwoo.core.dto.PageResponse
import com.pollyannawu.justwoo.core.dto.TaskResponse
import com.pollyannawu.justwoo.core.TaskStatus
import kotlin.math.ceil

interface TaskService {
    suspend fun getTaskDetails(
        houseId: Long,
        userId: Long,
        taskStatus: TaskStatus? = null,
        page: Int
    ): TaskDataResult<PageResponse<TaskResponse>>

    suspend fun getTasksByOwnerId(
        houseId: Long,
        userId: Long,
        ownerId: Long,
        taskStatus: TaskStatus? = null,
        page: Int
    ): TaskDataResult<PageResponse<TaskResponse>>

    suspend fun getTasksByAssigneeId(
        houseId: Long,
        userId: Long,
        assigneeId: Long,
        taskStatus: TaskStatus? = null,
        page: Int
    ): TaskDataResult<PageResponse<TaskResponse>>

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
        ownerId: Long,
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

    private suspend fun mergeToPageResponse(tasks: List<Task>, totalCount: Long, page: Int): PageResponse<TaskResponse> {
        val assigneesId = tasks.flatMap { it.assignees }.map { it.userId }.distinct()
        val profilesMap = profileRepo.getProfiles(assigneesId).associateBy { it.id }

        val taskResponses = tasks.map { task ->
            val taskAssigneeResponses = task.assignees.mapNotNull { assignee ->
                profilesMap[assignee.userId]?.let { assignee.toResponse(it) }
            }
            task.toResponse(taskAssigneeResponses)
        }

        val totalPages = ceil(totalCount.toDouble() / TASK_PAGE_SIZE).toInt()
        val isLast = (page.toLong() * TASK_PAGE_SIZE) >= totalCount

        return PageResponse(
            content = taskResponses,
            pageNumber = page,
            pageSize = TASK_PAGE_SIZE,
            totalElements = totalCount,
            totalPages = totalPages,
            isLast = isLast
        )
    }

    private suspend fun mergeTaskAssigneeResponse(task: Task): TaskResponse {
        val profilesMap = profileRepo.getProfiles(task.assignees.map { it.userId }).associateBy { it.id }
        val assigneeResponses = task.assignees.mapNotNull { assignee ->
            profilesMap[assignee.userId]?.let { assignee.toResponse(it) }
        }
        return task.toResponse(assigneeResponses)
    }

    override suspend fun getTaskDetails(
        houseId: Long,
        userId: Long,
        taskStatus: TaskStatus?,
        page: Int
    ): TaskDataResult<PageResponse<TaskResponse>> {
        if (!houseRepo.isMember(userId, houseId)) return TaskDataResult.Error.UserNotAllowed(
            userId,
            TaskUserType.REQUEST
        )
        val offset = (page - 1).toLong() * TASK_PAGE_SIZE
        val pagedTasks = taskRepo.getTasks(houseId, taskStatus, TASK_PAGE_SIZE, offset)
        return TaskDataResult.Success(mergeToPageResponse(pagedTasks.items, pagedTasks.totalCount, page))
    }

    override suspend fun getTasksByOwnerId(
        houseId: Long,
        userId: Long,
        ownerId: Long,
        taskStatus: TaskStatus?,
        page: Int
    ): TaskDataResult<PageResponse<TaskResponse>> {
        if (!houseRepo.isMember(ownerId, houseId)) return TaskDataResult.Error.UserNotAllowed(
            ownerId,
            TaskUserType.OWNER
        )
        val offset = (page - 1).toLong() * TASK_PAGE_SIZE
        val pagedTasks = taskRepo.getTasksByOwnerId(houseId, ownerId, taskStatus, TASK_PAGE_SIZE, offset)
        return TaskDataResult.Success(mergeToPageResponse(pagedTasks.items, pagedTasks.totalCount, page))
    }

    override suspend fun getTasksByAssigneeId(
        houseId: Long,
        userId: Long,
        assigneeId: Long,
        taskStatus: TaskStatus?,
        page: Int
    ): TaskDataResult<PageResponse<TaskResponse>> {
        if (!houseRepo.isMember(assigneeId, houseId)) return TaskDataResult.Error.UserNotAllowed(
            assigneeId,
            TaskUserType.ASSIGNEE
        )
        val offset = (page - 1).toLong() * TASK_PAGE_SIZE
        val pagedTasks = taskRepo.getTasksByAssigneeId(houseId, assigneeId, taskStatus, TASK_PAGE_SIZE, offset)
        return TaskDataResult.Success(mergeToPageResponse(pagedTasks.items, pagedTasks.totalCount, page))
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
        return try {
            val updatedTask = taskRepo.updateTaskAssignStatus(taskId, assignee.userId, assignee.status)
            if (assignee.status == AssignStatus.ACCEPTED) {
                val result = taskRepo.updateTaskExecutor(assignee.userId, taskId)
                TaskDataResult.Success(mergeTaskAssigneeResponse(result))
            } else {
                TaskDataResult.Success(mergeTaskAssigneeResponse(updatedTask))
            }
        } catch (e: Exception) {
            TaskDataResult.Error.DatabaseError(
                e.message ?: "Unknown Exception when updateTaskAssignStatus"
            )
        }
    }

    override suspend fun updateTaskStatus(
        houseId: Long,
        ownerId: Long,
        assigneeId: Long,
        taskId: Long,
        taskStatus: TaskStatus
    ): TaskDataResult<TaskResponse> {
        if (!houseRepo.isMember(assigneeId, houseId) || !taskRepo.isTaskOwnerOrExecutor(
                ownerId,
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

    companion object {
        private const val TASK_PAGE_SIZE = 20
    }
}