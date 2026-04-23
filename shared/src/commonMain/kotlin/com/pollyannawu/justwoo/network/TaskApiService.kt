package com.pollyannawu.justwoo.network

import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.core.dto.PageResponse
import com.pollyannawu.justwoo.core.dto.TaskResponse
import com.pollyannawu.justwoo.model.ApiResult

interface TaskApiService {
    suspend fun getTasks(houseId: Long, page: Int): ApiResult<PageResponse<TaskResponse>>
    suspend fun getTaskById(taskId: Long): ApiResult<TaskResponse>
    suspend fun getTasksByDateRange(startDate: String, endDate: String): ApiResult<List<TaskResponse>>
    suspend fun createTask(request: CreateTaskRequest): ApiResult<TaskResponse>
    suspend fun updateTask(taskId: Long, request: CreateTaskRequest): ApiResult<TaskResponse>
    suspend fun deleteTask(taskId: Long): ApiResult<Unit>
}
