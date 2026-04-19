package com.pollyannawu.justwoo.network

import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.core.dto.TokenResponse
import com.pollyannawu.justwoo.model.ApiResult

interface AuthApiService {
    suspend fun loginByEmailAndPassword(
        email: String,
        plainPassword: String,
        deviceId: String
    ): ApiResult<AuthResponse?>

    suspend fun refresh(deviceId: String, token: String): ApiResult<TokenResponse?>
    suspend fun register(email: String, plainPassword: String, deviceId: String): ApiResult<AuthResponse?>
    suspend fun delete(userId: String, confirmPassword: String): ApiResult<Nothing>
}