package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.data.datasource.auth.DeviceIdProvider
import com.pollyannawu.justwoo.data.datasource.auth.TokenStorage
import com.pollyannawu.justwoo.data.datasource.auth.UserStorage
import com.pollyannawu.justwoo.model.ApiResult
import com.pollyannawu.justwoo.model.AuthDataResult
import com.pollyannawu.justwoo.data.network.data.AuthToken
import com.pollyannawu.justwoo.data.network.service.AuthApiService
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map


interface AuthRepository {
    val currentUserId: Flow<Long?>
    val currentHouseId: Flow<Long?>

    val isAuthenticated: Flow<Boolean>

    fun hasActiveSession(): Boolean
    fun getCurrentHouseId(): Long?
    fun hasOnboarded(): Boolean

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

    override val isAuthenticated: Flow<Boolean> =
        combine(userStorage.userFlow, tokenStorage.tokensFlow) { user, tokens ->
            user != null && tokens != null
        }.distinctUntilChanged()

    override fun hasActiveSession(): Boolean =
        userStorage.getUser() != null && tokenStorage.getTokens() != null

    override fun getCurrentHouseId(): Long? = userStorage.getHouseId()

    override fun hasOnboarded(): Boolean = userStorage.hasOnboarded()

    override fun setCurrentHouseId(houseId: Long) {
        userStorage.saveHouseId(houseId)
    }

    override suspend fun register(email: String, password: String): AuthDataResult {
        val deviceId = deviceIdProvider.get()
        return apiService.register(email, password, deviceId)
            .toAuthDataResult(AuthApiFlow.Register)
    }

    override suspend fun login(email: String, password: String): AuthDataResult {
        val deviceId = deviceIdProvider.get()
        return apiService.loginByEmailAndPassword(email, password, deviceId)
            .toAuthDataResult(AuthApiFlow.Login)
    }

    private enum class AuthApiFlow { Login, Register }

    /**
     * Translate ApiResult → AuthDataResult, mapping HTTP status codes to
     * domain failures so the use-case / UI layer can react without
     * inspecting Ktor exceptions.
     *
     * Status code conventions come from [AuthRoutes.respondAuthResult]:
     *  - 401 → PasswordError
     *  - 409 → HasRegisteredUser
     *  - 429 → OutOfLoginAttemptLimit
     *  - 400 → catch-all (incl. login's HasNoRegisteredUser)
     */
    private fun ApiResult<AuthResponse?>.toAuthDataResult(flow: AuthApiFlow): AuthDataResult {
        return when (this) {
            is ApiResult.Success -> data?.let {
                persistAuth(it)
                AuthDataResult.Success(it.user)
            } ?: AuthDataResult.Failure.NetworkFailure

            is ApiResult.Error -> mapError(exception, flow)
            is ApiResult.Loading -> AuthDataResult.Failure.NetworkFailure
        }
    }

    private fun mapError(throwable: Throwable, flow: AuthApiFlow): AuthDataResult.Failure {
        val status = (throwable as? ClientRequestException)?.response?.status
            ?: return AuthDataResult.Failure.NetworkFailure

        return when (status) {
            HttpStatusCode.Unauthorized -> AuthDataResult.Failure.InvalidCredentials
            HttpStatusCode.Conflict -> AuthDataResult.Failure.EmailAlreadyRegistered
            HttpStatusCode.TooManyRequests -> AuthDataResult.Failure.TooManyAttempts
            HttpStatusCode.BadRequest -> when (flow) {
                // Backend currently returns 400 for wrong-password / no-user on login.
                AuthApiFlow.Login -> AuthDataResult.Failure.InvalidCredentials
                AuthApiFlow.Register -> AuthDataResult.Failure.InvalidRequest
            }
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
        userStorage.markOnboarded()
    }
}
