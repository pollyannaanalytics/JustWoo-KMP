package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.data.datasource.auth.DeviceIdProvider
import com.pollyannawu.justwoo.data.datasource.auth.TokenStorage
import com.pollyannawu.justwoo.data.datasource.auth.UserStorage
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.model.AuthDataResult
import com.pollyannawu.justwoo.data.network.data.AuthToken
import com.pollyannawu.justwoo.data.network.service.AuthApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


interface AuthRepository {
    val currentUserId: Flow<Long?>
    val currentHouseId: Flow<Long?>

    suspend fun register(email: String, password: String): AuthDataResult
    suspend fun login(email: String, password: String): AuthDataResult
    suspend fun logout()

    fun setCurrentHouseId(houseId: Long)
}

class DefaultAuthRepository(
    private val apiService: AuthApiService,
    private val tokenStorage: TokenStorage,
    private val userStorage: UserStorage,
    private val deviceIdProvider: DeviceIdProvider,
) : AuthRepository {

    override val currentUserId: Flow<Long?> = userStorage.userFlow.map { it?.id }
    override val currentHouseId: Flow<Long?> = userStorage.houseIdFlow

    override fun setCurrentHouseId(houseId: Long) {
        userStorage.saveHouseId(houseId)
    }

    override suspend fun register(email: String, password: String): AuthDataResult {
        val deviceId = deviceIdProvider.get()

        return when (val result = apiService.register(email, password, deviceId)) {
            is ApiResult.Success -> {
                result.data?.let { data ->
                    persistAuth(data)
                    AuthDataResult.Success(data.user)
                } ?: AuthDataResult.Failure.NetworkFailure
            }

            is ApiResult.Error -> AuthDataResult.Failure.NetworkFailure
            is ApiResult.Loading -> AuthDataResult.Failure.NetworkFailure
        }
    }

    override suspend fun login(email: String, password: String): AuthDataResult {
        val deviceId = deviceIdProvider.get()
        return when (val result = apiService.loginByEmailAndPassword(email, password, deviceId)) {
            is ApiResult.Success -> {
                result.data?.let { data ->
                    persistAuth(data)
                    AuthDataResult.Success(data.user)
                } ?: AuthDataResult.Failure.NetworkFailure
            }

            is ApiResult.Error -> AuthDataResult.Failure.NetworkFailure
            else -> AuthDataResult.Failure.NetworkFailure
        }
    }

    override suspend fun logout() {
        tokenStorage.clear()
        userStorage.clear()
    }

    private fun persistAuth(data: AuthResponse) {
        tokenStorage.saveTokens(
            AuthToken(
                accessToken = data.accessToken,
                refreshToken = data.token.refreshToken,
            )
        )
        userStorage.saveUser(data.user)
    }
}
