package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.datasource.AuthDataSource
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.model.AuthDataResult
import com.pollyannawu.justwoo.network.AuthApiService


interface AuthRepository {
    suspend fun register(email: String, password: String): AuthDataResult
    suspend fun login(email: String, password: String): AuthDataResult
    suspend fun logout()
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
                } ?: AuthDataResult.Failure.NetworkFailure
            }

            is ApiResult.Error -> AuthDataResult.Failure.NetworkFailure
            is ApiResult.Loading -> AuthDataResult.Failure.NetworkFailure
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

    private suspend fun saveUserInLocal(data: AuthResponse) {
        localDataSource.saveAccessToken(data.accessToken)
        localDataSource.saveRefreshToken(data.token.refreshToken)
        localDataSource.saveUser(data.user)
    }
}