package com.pollyannawu.justwoo.backend.utils.mapper

import com.pollyannawu.justwoo.core.dto.User as UserResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


val now = Clock.System.now()
@Serializable
data class User(
    val id: Long,
    val email: String,
    val passwordHash: String,
    val userRefreshTokenId: Long,
    val createTime: Instant,
    val updateTime: Instant
)

fun User.toResponse(): UserResponse = UserResponse(
    id = this.id,
    email = this.email,
    createTime = now
)
