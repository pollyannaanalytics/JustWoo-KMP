package com.pollyannawu.justwoo.backend.utils.mapper

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.dto.HouseRequest
import com.pollyannawu.justwoo.core.dto.HouseResponse
import com.pollyannawu.justwoo.core.dto.MemberProfileResponse
import kotlinx.datetime.Clock


fun House.toResponse(members: List<MemberProfileResponse>): HouseResponse {
    return HouseResponse(
        id = this.id,
        title = this.name,
        name = this.name,
        members = members,
        avatarUrl = this.avatar,
        description = this.description,
        createTime = this.createTime,
        updateTime = this.updateTime
    )
}



fun HouseRequest.toDomain(id: Long = 0): House {
    val now = Clock.System.now()
    return House(
        id = id,
        name = this.name,
        description = this.description,
        avatar = this.avatar ?: "",
        members = this.memberIds.filter { it > 0 }.map { memberId ->
            HouseMember(
                houseId = 0,
                userId = memberId,
                role = MemberRole.MEMBER,
                joinedAt = now
            )
        },
        createTime = this.createTime,
        updateTime = this.updateTime
    )
}

