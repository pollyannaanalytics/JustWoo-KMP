package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.auth.AuthRepository
import com.pollyannawu.justwoo.backend.repositories.auth.LoginAttemptRepository
import com.pollyannawu.justwoo.backend.repositories.auth.RefreshTokenRepository
import com.pollyannawu.justwoo.backend.utils.dataresult.AuthDataResult
import com.pollyannawu.justwoo.backend.utils.mapper.toResponse
import com.pollyannawu.justwoo.backend.utils.security.AccessTokenProvider
import com.pollyannawu.justwoo.backend.utils.security.HashPasswordProvider
import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.core.dto.TokenResponse
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days


interface AuthService {
    suspend fun loginByEmailAndPassword(
        email: String,
        plainPassword: String,
        deviceId: String
    ): AuthDataResult<AuthResponse>

    suspend fun refresh(deviceId: String, token: String): AuthDataResult<TokenResponse>
    suspend fun register(email: String, plainPassword: String, deviceId: String): AuthDataResult<AuthResponse>
    suspend fun delete(userId: String, confirmPassword: String): AuthDataResult<Nothing>
}


class DefaultAuthService(
    private val authRepository: AuthRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val loginAttemptRepository: LoginAttemptRepository,
    private val hashPasswordProvider: HashPasswordProvider,
    private val accessTokenProvider: AccessTokenProvider,

    ) : AuthService {

    override suspend fun loginByEmailAndPassword(
        email: String,
        plainPassword: String,
        deviceId: String
    ): AuthDataResult<AuthResponse> {
        if ((loginAttemptRepository.getLoginAttempt(email, LOGIN_ATTEMPTS_EXPIRATION_IN_SECONDS)?.count
                ?: 0) > MAX_LOGIN_ATTEMPTS
        ) return AuthDataResult.Failure.OutOfLoginAttemptLimit
        val user = authRepository.findUserByAccount(email)
        return user?.let {
            loginAttemptRepository.recordLoginAttempt(email, LOGIN_ATTEMPTS_EXPIRATION_IN_SECONDS)
            val hashedPassword = it.passwordHash
            if (!hashPasswordProvider.validatePassword(plainPassword, hashedPassword)) {
                return AuthDataResult.Failure.HasNoRegisteredUser(
                    email = email,
                )
            }

            val refreshToken = refreshTokenRepository.saveToken(
                userId = user.id,
                deviceId = deviceId,
                expireDuration = REFRESH_TOKEN_EXPIRATION_IN_SECONDS
            )
            val jwtToken = accessTokenProvider.createAccessToken(user.id)
            AuthDataResult.Success(
                AuthResponse(
                    it.toResponse(),
                    jwtToken,
                    TokenResponse(refreshToken.token, refreshToken.expiresAt.toInstant())
                )
            )
        } ?: run {
            AuthDataResult.Failure.HasNoRegisteredUser(
                email = email,
            )
        }
    }


    override suspend fun refresh(deviceId: String, token: String): AuthDataResult<TokenResponse> {
        try {
            val originalToken = refreshTokenRepository.findToken(token)
            if (originalToken == null || originalToken.device != deviceId) {
                return AuthDataResult.Failure.Unauthorized
            }
            val refreshToken = refreshTokenRepository.saveToken(originalToken.userId, originalToken.device, expireDuration = REFRESH_TOKEN_EXPIRATION_IN_SECONDS)
            
            return AuthDataResult.Success(
                TokenResponse(
                    refreshToken.token,
                    refreshToken.expiresAt.toInstant()
                )
            )
        } catch (e: Exception) {
            return AuthDataResult.Failure.InvalidData
        }
    }


    override suspend fun register(
        email: String,
        plainPassword: String,
        deviceId: String
    ): AuthDataResult<AuthResponse> {
        if (authRepository.findUserByAccount(email) != null) {
            return AuthDataResult.Failure.HasRegisteredUser
        }
        val hashPassword = hashPasswordProvider.hashPassword(plainPassword)
        val user = authRepository.create(email, hashPassword)
        val accessToken = accessTokenProvider.createAccessToken(user.id)
        val refreshToken =
            refreshTokenRepository.saveToken(user.id, deviceId, REFRESH_TOKEN_EXPIRATION_IN_SECONDS)
        return AuthDataResult.Success(
            AuthResponse(
                user.toResponse(),
                accessToken,
                TokenResponse(refreshToken.token, refreshToken.expiresAt.toInstant())
            )
        )
    }


    private fun Long.toInstant(): Instant {
        return Instant.fromEpochSeconds(this)
    }

    override suspend fun delete(userId: String, confirmPassword: String): AuthDataResult<Nothing> {
        val hashedPassword = authRepository.getUser(userId).passwordHash
        if (!hashPasswordProvider.validatePassword(confirmPassword, hashedPassword)) {
            return AuthDataResult.Failure.PasswordError
        }

        authRepository.delete(userId)
        refreshTokenRepository.deleteAllTokensByUser(userId)
        return AuthDataResult.Success(
            data = null
        )
    }



    companion object {
        private const val MAX_LOGIN_ATTEMPTS = 10
        private val REFRESH_TOKEN_EXPIRATION_IN_SECONDS = 7.days
        private val LOGIN_ATTEMPTS_EXPIRATION_IN_SECONDS = 1.days

    }

}