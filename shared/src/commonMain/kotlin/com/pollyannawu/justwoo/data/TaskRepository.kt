package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.core.toTask
import com.pollyannawu.justwoo.datasource.AuthDataSource
import com.pollyannawu.justwoo.datasource.TaskDataSource
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.network.TaskApiService
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun refreshTasks(houseId: Long, page: Int)
    suspend fun getTaskById(taskId: Long): Task?
    suspend fun getTasksByDateRange(startDate: String, endDate: String): Flow<List<Task>>
    suspend fun createTask(request: CreateTaskRequest)
    suspend fun updateTask(taskId: Long, request: CreateTaskRequest)
    suspend fun deleteTask(taskId: Long)
}

class DefaultTaskRepository(
    private val authDataSource: AuthDataSource,
    private val taskApiService: TaskApiService,
    private val taskDataSource: TaskDataSource,
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> = taskDataSource.getTasks()

    override suspend fun refreshTasks(houseId: Long, page: Int) {
        authDataSource.getUser() ?: return
        val result = taskApiService.getTasks(houseId, page)
        if (result is ApiResult.Success) {
            val tasks = result.data.content.map { it.toTask() }
            taskDataSource.updateTasks(tasks)
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun getTaskById(taskId: Long): Task? {
        authDataSource.getUser() ?: return null
        val cached = taskDataSource.getTaskById(taskId)
        if (cached != null) return cached
        val result = taskApiService.getTaskById(taskId)
        if (result is ApiResult.Success) {
            val task = result.data.toTask()
            taskDataSource.saveTask(task)
            return task
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
        return null
    }

    override suspend fun getTasksByDateRange(startDate: String, endDate: String): Flow<List<Task>> {
        authDataSource.getUser() ?: return taskDataSource.getTasks()
        val result = taskApiService.getTasksByDateRange(startDate, endDate)
        if (result is ApiResult.Success) {
            val tasks = result.data.map { it.toTask() }
            taskDataSource.updateTasks(tasks)
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
        return taskDataSource.getTasks()
    }

    override suspend fun createTask(request: CreateTaskRequest) {
        authDataSource.getUser() ?: return
        val result = taskApiService.createTask(request)
        if (result is ApiResult.Success) {
            taskDataSource.saveTask(result.data.toTask())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun updateTask(taskId: Long, request: CreateTaskRequest) {
        authDataSource.getUser() ?: return
        val result = taskApiService.updateTask(taskId, request)
        if (result is ApiResult.Success) {
            taskDataSource.updateTask(result.data.toTask())
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        authDataSource.getUser() ?: return
        val result = taskApiService.deleteTask(taskId)
        if (result is ApiResult.Success) {
            taskDataSource.deleteTask(taskId)
        } else if (result is ApiResult.Error) {
            throw Exception(result.exception)
        }
    }
}
