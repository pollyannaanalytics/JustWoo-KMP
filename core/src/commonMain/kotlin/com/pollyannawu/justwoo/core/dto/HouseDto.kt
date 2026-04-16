package com.pollyannawu.justwoo.core.dto

import com.pollyannawu.justwoo.core.MemberRole
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class HouseRequest(
    val title: String,
    val name: String,
    val adminUserId: Long,
    val memberIds: List<Long>,
    val description: String,
    val createTime: Instant,
    val updateTime: Instant
    )


@Serializable
data class HouseResponse(
    val id: Long,
    val title: String,
    val name: String,
    val members: List<MemberProfileResponse>,
    val description: String,
    val createTime: Instant,
    val updateTime: Instant
)

@Serializable
data class MemberProfileResponse(
    val profile: ProfileResponse,
    val role: MemberRole
)