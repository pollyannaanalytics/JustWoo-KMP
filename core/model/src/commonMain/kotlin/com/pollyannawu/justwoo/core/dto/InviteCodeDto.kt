package com.pollyannawu.justwoo.core.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class InviteCodeResponse(
    val code: String,
    val expiresAt: Instant,
    val houseId: Long,
)
