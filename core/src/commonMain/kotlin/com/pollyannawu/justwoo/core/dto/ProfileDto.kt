package com.pollyannawu.justwoo.core.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val id: Long,
    val name: String,
    val avatar: String,
    val updateTime: Instant
)