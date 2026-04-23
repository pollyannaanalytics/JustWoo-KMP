package com.pollyannawu.justwoo.android.stub

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.core.dto.HouseResponse
import com.pollyannawu.justwoo.core.dto.PageResponse
import com.pollyannawu.justwoo.core.dto.TaskResponse
import com.pollyannawu.justwoo.core.dto.TokenResponse
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.network.AuthApiService
import com.pollyannawu.justwoo.network.HouseApiService
import com.pollyannawu.justwoo.network.TaskApiService

/**
 * ---------------------------------------------------------------------------
 *  TODO: replace these stubs with real Ktor-backed implementations in
 *  :shared (commonMain). Kept here so the :androidApp module compiles and
 *  can be run against mocked data today.
 * ---------------------------------------------------------------------------
 */

class StubAuthApiService : AuthApiService {
    override suspend fun loginByEmailAndPassword(email: String, plainPassword: String, deviceId: String): ApiResult<AuthResponse?> =
        ApiResult.Error(Throwable("StubAuthApiService.loginByEmailAndPassword not implemented"))
    override suspend fun refresh(deviceId: String, token: String): ApiResult<TokenResponse?> =
        ApiResult.Error(Throwable("StubAuthApiService.refresh not implemented"))
    override suspend fun register(email: String, plainPassword: String, deviceId: String): ApiResult<AuthResponse?> =
        ApiResult.Error(Throwable("StubAuthApiService.register not implemented"))
    override suspend fun delete(userId: String, confirmPassword: String): ApiResult<Nothing> =
        ApiResult.Error(Throwable("StubAuthApiService.delete not implemented"))
}

class StubHouseApiService : HouseApiService {
    override suspend fun getHouses(userId: Long): ApiResult<List<HouseResponse>> =
        ApiResult.Success(emptyList())
    override suspend fun createHouse(userId: Long, house: House): ApiResult<HouseResponse> =
        ApiResult.Error(Throwable("StubHouseApiService.createHouse not implemented"))
    override suspend fun updateHouse(houseId: Long, userId: Long): ApiResult<HouseResponse> =
        ApiResult.Error(Throwable("StubHouseApiService.updateHouse not implemented"))
    override suspend fun getHouseById(id: Long, userId: Long): ApiResult<HouseResponse> =
        ApiResult.Error(Throwable("StubHouseApiService.getHouseById not implemented"))
}

class StubTaskApiService : TaskApiService {
    override suspend fun getTasks(houseId: Long, page: Int): ApiResult<PageResponse<TaskResponse>> =
        ApiResult.Success(
            PageResponse(
                content = emptyList(),
                pageNumber = page,
                pageSize = 0,
                totalElements = 0L,
                totalPages = 0,
                isLast = true,
            )
        )
    override suspend fun getTaskById(taskId: Long): ApiResult<TaskResponse> =
        ApiResult.Error(Throwable("StubTaskApiService.getTaskById not implemented"))
    override suspend fun getTasksByDateRange(startDate: String, endDate: String): ApiResult<List<TaskResponse>> =
        ApiResult.Success(emptyList())
    override suspend fun createTask(request: CreateTaskRequest): ApiResult<TaskResponse> =
        ApiResult.Error(Throwable("StubTaskApiService.createTask not implemented"))
    override suspend fun updateTask(taskId: Long, request: CreateTaskRequest): ApiResult<TaskResponse> =
        ApiResult.Error(Throwable("StubTaskApiService.updateTask not implemented"))
    override suspend fun deleteTask(taskId: Long): ApiResult<Unit> =
        ApiResult.Success(Unit)
}
