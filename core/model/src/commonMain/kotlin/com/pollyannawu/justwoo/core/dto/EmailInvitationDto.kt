package com.pollyannawu.justwoo.core.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EmailInvitationRequest(
    val email: String,
)

@Serializable
data class EmailInvitationResponse(
    val id: Long,
    val houseId: Long,
    val houseName: String,
    val houseAvatar: String,
    val code: String,
    val expiresAt: Instant,
)
