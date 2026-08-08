package com.pollyannawu.justwoo.core.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpInviteSession(
    val code: String,
    val displayNumber: Int,
    val expiresInSeconds: Int,
)
