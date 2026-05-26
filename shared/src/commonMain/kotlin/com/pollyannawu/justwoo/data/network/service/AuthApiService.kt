package com.pollyannawu.justwoo.data.network.service

import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.core.dto.LoginRequest
import com.pollyannawu.justwoo.core.dto.RegisterRequest
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.data.network.safeApiCall
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
    ): ApiResult<AuthResponse?> = safeApiCall(tag = "AuthApi.login") {
        client.post("/auth/login") {
            setBody(LoginRequest(email = email, password = plainPassword, deviceId = deviceId))
        }.body()
    }

    override suspend fun register(
        email: String,
        plainPassword: String,
        deviceId: String,
    ): ApiResult<AuthResponse?> = safeApiCall(tag = "AuthApi.register") {
        client.post("/auth/register") {
            setBody(RegisterRequest(email = email, plainPassword = plainPassword, deviceId = deviceId))
        }.body()
    }

    override suspend fun delete(
        userId: String,
        confirmPassword: String,
    ): ApiResult<Unit> = safeApiCall(tag = "AuthApi.delete") {
        client.delete("/auth/$userId")
        Unit
    }
}
