package com.pollyannawu.justwoo.core

import com.pollyannawu.justwoo.core.dto.HouseResponse
import com.pollyannawu.justwoo.core.dto.MemberProfileResponse
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class House(
    val id: Long,
    val name: String,
    val avatar: String,
    val description: String,
    val members: List<HouseMember>,
    val createTime: Instant,
    val updateTime: Instant
)

@Serializable
data class HouseMember(
    val houseId: Long,
    val userId: Long,
    val role: MemberRole,
    val joinedAt: Instant
)

fun HouseResponse.toHouse() = House(
    id = this.id,
    name = this.name,
    description = this.description,
    avatar = this.avatarUrl,
    members = this.members.toMembers(houseId = this.id),
    createTime = this.createTime,
    updateTime = this.updateTime,
)

fun List<MemberProfileResponse>.toMembers(houseId: Long): List<HouseMember> =
    map {
        HouseMember(
            userId = it.profile.id,
            houseId = houseId,
            role = it.role,
            joinedAt = it.joinAt
        )
    }
