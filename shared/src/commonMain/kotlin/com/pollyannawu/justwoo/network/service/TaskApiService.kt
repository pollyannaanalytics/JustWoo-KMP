package com.pollyannawu.justwoo.network

import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.core.dto.PageResponse
import com.pollyannawu.justwoo.core.dto.TaskResponse
import com.pollyannawu.justwoo.model.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface TaskApiService {
    suspend fun getTasks(houseId: Long, page: Int = 1): ApiResult<PageResponse<TaskResponse>>
    suspend fun createTask(request: CreateTaskRequest): ApiResult<TaskResponse>
    suspend fun updateTask(houseId: Long, task: Task): ApiResult<TaskResponse>
    suspend fun updateTaskAssignStatus(houseId: Long, taskId: Long, assignee: TaskAssignee): ApiResult<TaskResponse>
    suspend fun updateTaskStatus(houseId: Long, taskId: Long, status: TaskStatus): ApiResult<TaskResponse>
}

class DefaultTaskApiService(
    private val client: HttpClient,
) : TaskApiService {

    override suspend fun getTasks(houseId: Long, page: Int): ApiResult<PageResponse<TaskResponse>> = safeApiCall {
        client.get("/houses/$houseId/tasks") {
            parameter("page", page)
        }.body()
    }

    override suspend fun createTask(request: CreateTaskRequest): ApiResult<TaskResponse> = safeApiCall {
        client.post("/houses/${request.houseId}/tasks") {
            setBody(request)
        }.body()
    }

    override suspend fun updateTask(houseId: Long, task: Task): ApiResult<TaskResponse> = safeApiCall {
        client.patch("/houses/$houseId/tasks/${task.id}") {
            setBody(task)
        }.body()
    }

    override suspend fun updateTaskAssignStatus(
        houseId: Long,
        taskId: Long,
        assignee: TaskAssignee,
    ): ApiResult<TaskResponse> = safeApiCall {
        client.patch("/houses/$houseId/tasks/$taskId/assignees/${assignee.userId}") {
            setBody(assignee.status)
        }.body()
    }

    override suspend fun updateTaskStatus(
        houseId: Long,
        taskId: Long,
        status: TaskStatus,
    ): ApiResult<TaskResponse> = safeApiCall {
        client.patch("/houses/$houseId/tasks/$taskId/status") {
            setBody(status)
        }.body()
    }
}
