package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.datasource.AuthDataSource
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.model.AuthDataResult
import com.pollyannawu.justwoo.model.RefreshTokenResult
import com.pollyannawu.justwoo.network.AuthApiService


interface AuthRepository {
    suspend fun register(email: String, password: String): AuthDataResult
    suspend fun login(email: String, password: String): AuthDataResult
    suspend fun logout()
    suspend fun refreshToken(): RefreshTokenResult
}

class DefaultAuthRepository(
    private val apiService: AuthApiService,
    private val localDataSource: AuthDataSource
) : AuthRepository {

    override suspend fun register(email: String, password: String): AuthDataResult {
        val deviceId = localDataSource.getDeviceId()

        return when (val result = apiService.register(email, password, deviceId)) {
            is ApiResult.Success -> {
                result.data?.let { data ->
                    saveUserInLocal(data)
                    AuthDataResult.Success(data.user)
                } ?: AuthDataResult.Failure.NetworkFailure // 或自定義 DataParsingError
            }

            is ApiResult.Error -> AuthDataResult.Failure.NetworkFailure
            is ApiResult.Loading -> AuthDataResult.Failure.NetworkFailure // Loading 在 suspend 中不應作為結果
        }
    }

    override suspend fun login(email: String, password: String): AuthDataResult {
        val deviceId = localDataSource.getDeviceId()
        return when (val result = apiService.loginByEmailAndPassword(email, password, deviceId)) {
            is ApiResult.Success -> {
                result.data?.let { data ->
                    saveUserInLocal(data)
                    AuthDataResult.Success(data.user)
                } ?: AuthDataResult.Failure.NetworkFailure
            }

            is ApiResult.Error -> AuthDataResult.Failure.NetworkFailure
            else -> AuthDataResult.Failure.NetworkFailure
        }
    }

    override suspend fun logout() {
        localDataSource.clearAll()
    }

    override suspend fun refreshToken(): RefreshTokenResult {
        val deviceId = localDataSource.getDeviceId()
        val token = localDataSource.getToken() ?: return RefreshTokenResult.Failure.NoRefreshToken

        return when (val result = apiService.refresh(deviceId, token)) {
            is ApiResult.Success -> {
                val newToken = result.data?.refreshToken ?: return RefreshTokenResult.Failure.NetworkFailure
                localDataSource.saveRefreshToken(newToken)
                RefreshTokenResult.Success(newToken)
            }

            is ApiResult.Error -> RefreshTokenResult.Failure.NetworkFailure
            else -> RefreshTokenResult.Failure.NetworkFailure
        }
    }

    private suspend fun saveUserInLocal(data: AuthResponse) {
        localDataSource.saveAccessToken(data.accessToken)
        localDataSource.saveRefreshToken(data.token.refreshToken)
        localDataSource.saveUser(data.user)
    }
}