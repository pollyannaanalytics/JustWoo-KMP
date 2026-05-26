package com.pollyannawu.justwoo.core.dto

import com.pollyannawu.justwoo.core.JoinRequestStatus
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class JoinRequestBody(
    val inviteCode: String,
)

@Serializable
data class JoinRequestResponse(
    val id: Long,
    val houseId: Long,
    val userId: Long,
    val status: JoinRequestStatus,
    val createdAt: Instant,
)

@Serializable
data class JoinRequestDecision(
    val approve: Boolean,
)
