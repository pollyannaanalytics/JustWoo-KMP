package com.pollyannawu.justwoo.data.network.service

import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.core.dto.ChangePasswordRequest
import com.pollyannawu.justwoo.core.dto.LoginRequest
import com.pollyannawu.justwoo.core.dto.RegisterRequest
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.data.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

interface AuthApiService {
    suspend fun loginByEmailAndPassword(email: String, plainPassword: String, deviceId: String): ApiResult<AuthResponse?>
    suspend fun register(email: String, plainPassword: String, deviceId: String): ApiResult<AuthResponse?>
    suspend fun delete(userId: String, confirmPassword: String): ApiResult<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): ApiResult<Unit>
}

class DefaultAuthApiService(
    private val ktorClient: HttpClient,
) : AuthApiService {

    override suspend fun loginByEmailAndPassword(
        email: String,
        plainPassword: String,
        deviceId: String,
    ): ApiResult<AuthResponse?> = safeApiCall(tag = "AuthApi.login") {
        ktorClient.post("/auth/login") {
            setBody(LoginRequest(email = email, password = plainPassword, deviceId = deviceId))
        }.body()
    }

    override suspend fun register(
        email: String,
        plainPassword: String,
        deviceId: String,
    ): ApiResult<AuthResponse?> = safeApiCall(tag = "AuthApi.register") {
        ktorClient.post("/auth/register") {
            setBody(RegisterRequest(email = email, plainPassword = plainPassword, deviceId = deviceId))
        }.body()
    }

    override suspend fun delete(
        userId: String,
        confirmPassword: String,
    ): ApiResult<Unit> = safeApiCall(tag = "AuthApi.delete") {
        ktorClient.delete("/auth/$userId")
        Unit
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
    ): ApiResult<Unit> = safeApiCall(tag = "AuthApi.changePassword") {
        ktorClient.put("/auth/password") {
            setBody(ChangePasswordRequest(oldPassword = oldPassword, newPassword = newPassword))
        }
        Unit
    }
}
