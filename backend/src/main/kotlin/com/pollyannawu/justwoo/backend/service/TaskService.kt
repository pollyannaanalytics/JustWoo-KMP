package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.TaskRepository
import com.pollyannawu.justwoo.backend.utils.DataResult
import com.pollyannawu.justwoo.backend.utils.UserType
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus

interface TaskService {
    suspend fun getTasks(houseId: Long, userId: Long, taskStatus: TaskStatus? = null): DataResult<List<Task>>
    suspend fun getTasksByOwnerId(houseId: Long, userId: Long, ownerId: Long, taskStatus: TaskStatus? = null): DataResult<List<Task>>
    suspend fun getTasksByAssigneeId(houseId: Long, userId: Long, assigneeId: Long, taskStatus: TaskStatus? = null): DataResult<List<Task>>
    suspend fun createTask(houseId: Long, userId: Long, task: Task): DataResult<Task>
    suspend fun updateTaskContent(houseId: Long, ownerId: Long, task: Task): DataResult<Task>
    suspend fun updateTaskAssignStatus(houseId: Long, taskId: Long, assignee: TaskAssignee): DataResult<Task>
    suspend fun updateTaskStatus(houseId: Long, assigneeId: Long, taskId: Long, taskStatus: TaskStatus): DataResult<Task>
}

internal class DefaultTaskService(
    private val houseRepo: HouseRepository,
    private val taskRepo: TaskRepository
): TaskService{
    override suspend fun getTasks(
        houseId: Long,
        userId: Long,
        taskStatus: TaskStatus?
    ): DataResult<List<Task>> {
        if (!houseRepo.isMember(userId, houseId)) return DataResult.Error.UserNotMember(userId, UserType.REQUEST)
        return DataResult.Success(taskRepo.getTasks(houseId, taskStatus))
    }

    override suspend fun getTasksByOwnerId(
        houseId: Long,
        userId: Long,
        ownerId: Long,
        taskStatus: TaskStatus?
    ): DataResult<List<Task>> {
        if (!houseRepo.isMember(ownerId, houseId)) return DataResult.Error.UserNotMember(
            ownerId,
            UserType.OWNER
        )

        return getTasks(houseId, ownerId, taskStatus)
    }

    override suspend fun getTasksByAssigneeId(
        houseId: Long,
        userId: Long,
        assigneeId: Long,
        taskStatus: TaskStatus?
    ): DataResult<List<Task>> {
        if (!houseRepo.isMember(assigneeId, houseId)) return DataResult.Error.UserNotMember(
           assigneeId,
            UserType.ASSIGNEE
        )

        return getTasks(houseId, assigneeId, taskStatus)
    }

    override suspend fun createTask(houseId: Long, userId: Long, task: Task): DataResult<Task> {
        if (!houseRepo.isMember(userId, houseId)) return DataResult.Error.UserNotMember(
            userId,
            UserType.REQUEST
        )
        try {
            val result = taskRepo.createTask(task)
            return DataResult.Success(result)
        }catch (e: Exception){
            return DataResult.Error.DatabaseError(e.message ?: "Unknown Exception when createTask")
        }
    }

    override suspend fun updateTaskContent(houseId: Long, ownerId: Long, task: Task): DataResult<Task> {
        if (!houseRepo.isMember(ownerId, houseId)) return DataResult.Error.UserNotMember(
            ownerId,
            UserType.OWNER
        )
        try {
            val result = taskRepo.updateTaskContent(task)
            return DataResult.Success(result)
        }catch (e: Exception){
            return DataResult.Error.DatabaseError(e.message ?: "Unknown Exception when updateTaskContent")
        }
    }

    override suspend fun updateTaskAssignStatus(
        houseId: Long,
        taskId: Long,
        assignee: TaskAssignee
    ): DataResult<Task> {
        if (!houseRepo.isMember(assignee.userId, houseId)) return DataResult.Error.UserNotMember(
            assignee.userId,
            UserType.ASSIGNEE
        )
        try {
           if (assignee.status == AssignStatus.ACCEPTED) {
                taskRepo.updateTaskAssignStatus(taskId, assignee.userId, assignee.status)
               val result = taskRepo.updateTaskExecutor(assignee.userId, taskId)
                return DataResult.Success(result)
            }else{
                return DataResult.Error.AssigneeStatusError
            }
        }catch (e: Exception){
            return DataResult.Error.DatabaseError(e.message ?: "Unknown Exception when updateTaskAssignStatus")
        }
    }

    override suspend fun updateTaskStatus(
        houseId: Long,
        assigneeId: Long,
        taskId: Long,
        taskStatus: TaskStatus
    ): DataResult<Task> {
        if (!houseRepo.isMember(assigneeId, houseId) || !taskRepo.isTaskExecutor(assigneeId, taskId) ) return DataResult.Error.UserNotMember(
            assigneeId,
            UserType.ASSIGNEE
        )

        try {
            val result = taskRepo.updateTaskStatus(taskId, taskStatus)
            return DataResult.Success(result)
        }catch (e: Exception){
            return DataResult.Error.DatabaseError(e.message ?: "Unknown Exception when updateTaskAssignStatus")
        }
    }


}