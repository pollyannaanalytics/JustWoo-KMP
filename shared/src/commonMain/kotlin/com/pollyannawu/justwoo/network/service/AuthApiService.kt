package com.pollyannawu.justwoo.network

import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.core.dto.LoginRequest
import com.pollyannawu.justwoo.core.dto.RegisterRequest
import com.pollyannawu.justwoo.model.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface AuthApiService {
    suspend fun loginByEmailAndPassword(email: String, plainPassword: String, deviceId: String): ApiResult<AuthResponse?>
    suspend fun register(email: String, plainPassword: String, deviceId: String): ApiResult<AuthResponse?>
    suspend fun delete(userId: String, confirmPassword: String): ApiResult<Unit>
}

class DefaultAuthApiService(
    private val client: HttpClient,
) : AuthApiService {

    override suspend fun loginByEmailAndPassword(
        email: String,
        plainPassword: String,
        deviceId: String,
    ): ApiResult<AuthResponse?> = safeApiCall {
        client.post("/auth/login") {
            setBody(LoginRequest(email = email, password = plainPassword, deviceId = deviceId))
        }.body()
    }

    override suspend fun register(
        email: String,
        plainPassword: String,
        deviceId: String,
    ): ApiResult<AuthResponse?> = safeApiCall {
        client.post("/auth/register") {
            setBody(RegisterRequest(email = email, plainPassword = plainPassword, deviceId = deviceId))
        }.body()
    }

    override suspend fun delete(
        userId: String,
        confirmPassword: String,
    ): ApiResult<Unit> = safeApiCall {
        client.delete("/auth/$userId")
        Unit
    }
}
