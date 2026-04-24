package com.pollyannawu.justwoo.core.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val plainPassword: String,
    val deviceId: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceId: String,
)

@Serializable
data class RefreshRequest(
    val deviceId: String,
    val token: String,
)

@Serializable
data class AuthResponse(
    val user: User,
    val accessToken: String,
    val token: TokenResponse
)

@Serializable
data class User(
    val id: Long,
    val email: String,
    val createTime: Instant
)

@Serializable
data class TokenResponse(
    val refreshToken: String,
    val expireAt: Instant
)
