package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.core.toTask
import com.pollyannawu.justwoo.data.datasource.auth.UserStorage
import com.pollyannawu.justwoo.data.datasource.TaskDataSource
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.data.network.service.TaskApiService
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun refreshTasks(houseId: Long, page: Int = 1)
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun createTask(request: CreateTaskRequest)
    suspend fun updateTask(houseId: Long, task: Task)
    suspend fun updateTaskAssignStatus(houseId: Long, taskId: Long, assignee: TaskAssignee)
    suspend fun updateTaskStatus(houseId: Long, taskId: Long, status: TaskStatus)
}

class DefaultTaskRepository(
    private val userStorage: UserStorage,
    private val taskApiService: TaskApiService,
    private val taskDataSource: TaskDataSource,
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> = taskDataSource.getTasks()

    override suspend fun refreshTasks(houseId: Long, page: Int) {
        userStorage.getUser() ?: return
        val result = taskApiService.getTasks(houseId, page)
        if (result is ApiResult.Success) {
            val tasks = result.data.content.map { it.toTask() }
            taskDataSource.updateTasks(tasks)
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    // Cache-only — no backend GET /tasks/{id} route yet
    override suspend fun getTaskById(taskId: Long): Task? =
        taskDataSource.getTaskById(taskId)

    override suspend fun createTask(request: CreateTaskRequest) {
        userStorage.getUser() ?: return
        val result = taskApiService.createTask(request)
        if (result is ApiResult.Success) {
            taskDataSource.saveTask(result.data.toTask())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun updateTask(houseId: Long, task: Task) {
        userStorage.getUser() ?: return
        val result = taskApiService.updateTask(houseId, task)
        if (result is ApiResult.Success) {
            taskDataSource.updateTask(result.data.toTask())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun updateTaskAssignStatus(houseId: Long, taskId: Long, assignee: TaskAssignee) {
        userStorage.getUser() ?: return
        val result = taskApiService.updateTaskAssignStatus(houseId, taskId, assignee)
        if (result is ApiResult.Success) {
            taskDataSource.updateTask(result.data.toTask())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun updateTaskStatus(houseId: Long, taskId: Long, status: TaskStatus) {
        userStorage.getUser() ?: return
        val result = taskApiService.updateTaskStatus(houseId, taskId, status)
        if (result is ApiResult.Success) {
            taskDataSource.updateTask(result.data.toTask())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }
}
