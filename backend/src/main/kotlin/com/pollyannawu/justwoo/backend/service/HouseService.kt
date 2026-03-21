package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.ProfileRepository
import com.pollyannawu.justwoo.backend.utils.dataresult.HouseDataResult
import com.pollyannawu.justwoo.backend.utils.dataresult.HouseUserType
import com.pollyannawu.justwoo.backend.utils.mapper.toDomain
import com.pollyannawu.justwoo.backend.utils.mapper.toResponse
import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.dto.HouseRequest
import com.pollyannawu.justwoo.core.dto.HouseResponse
import com.pollyannawu.justwoo.core.dto.MemberProfileResponse
import kotlin.collections.mapNotNull

interface HouseService {
    suspend fun createHouse(request: HouseRequest): HouseDataResult<HouseResponse>
    suspend fun addMember(requesterId: Long, userId: Long, houseId: Long): HouseDataResult<HouseResponse>
    suspend fun removeMember(requesterId: Long, userId: Long, houseId: Long): HouseDataResult<HouseResponse>
    suspend fun updateHouseContent(
        userId: Long,
        houseId: Long,
        house: HouseRequest
    ): HouseDataResult<HouseResponse>

    suspend fun getHouses(userId: Long): HouseDataResult<List<HouseResponse>>
    suspend fun getHouse(userId: Long, houseId: Long): HouseDataResult<HouseResponse>
}

class DefaultHouseService(
    private val houseRepository: HouseRepository,
    private val profileRepository: ProfileRepository
) : HouseService {
    override suspend fun createHouse(request: HouseRequest): HouseDataResult<HouseResponse> {
        val newHouse = houseRepository.createHouse(request.toDomain(), request.adminUserId)
        return HouseDataResult.Success(
            mergeHouseAndProfileDetails(newHouse)
        )
    }

    override suspend fun addMember(requesterId: Long, userId: Long, houseId: Long): HouseDataResult<HouseResponse> =
        ensureMember(requesterId, houseId) {
            val house = houseRepository.addMember(userId, MemberRole.MEMBER, houseId)
            return HouseDataResult.Success(
                mergeHouseAndProfileDetails(house)
            )
        }

    override suspend fun removeMember(requesterId: Long, userId: Long, houseId: Long): HouseDataResult<HouseResponse> =
        ensureMember(requesterId, houseId) {
            val house = houseRepository.removeMember(userId, houseId)

            return HouseDataResult.Success(
                mergeHouseAndProfileDetails(house)
            )
        }

    override suspend fun updateHouseContent(
        userId: Long,
        houseId: Long,
        house: HouseRequest
    ): HouseDataResult<HouseResponse> = ensureMember(userId, houseId) {
        val house = houseRepository.updateHouseContent(house.toDomain(houseId))

        return HouseDataResult.Success(
            mergeHouseAndProfileDetails(house)
        )
    }

    override suspend fun getHouses(userId: Long): HouseDataResult<List<HouseResponse>>  {
        val houses = getHouseDetails(userId)
        val memberId = houses.flatMap { it.members }.map { it.userId }
        val profiles = profileRepository.getProfiles(memberId).associateBy { it.id }

        return HouseDataResult.Success(houses.map { house ->
            val members = house.members.map { it }
            val responses = members.mapNotNull { member ->
                val profileResponse = profiles[member.userId]?.toResponse() ?: return@mapNotNull null
                MemberProfileResponse(profile = profileResponse, role = member.role)
            }
            house.toResponse(responses)
        })
    }

    override suspend fun getHouse(
        userId: Long,
        houseId: Long
    ): HouseDataResult<HouseResponse> = ensureMember(userId, houseId) {
        return getHouseDetails(userId, houseId).takeIf { it.isNotEmpty() }?.let {
            val house = it.first()
            return HouseDataResult.Success(mergeHouseAndProfileDetails(house))
        } ?: run {
            HouseDataResult.Error.HouseNotFound
        }
    }

    private suspend fun getHouseDetails(userId: Long, houseId: Long? = null): List<House> {
        return houseRepository.getHouseDetails(userId, houseId)
    }

    private suspend fun mergeHouseAndProfileDetails(
        house: House
    ): HouseResponse {
        val memberIds = house.members.map { it.userId }.distinct()

        val members = house.members
        val profiles = profileRepository.getProfiles(memberIds).associateBy { it.id }

        val memberProfileResponses = members.mapNotNull { member ->
            val profileResponse = profiles[member.userId]?.toResponse() ?: return@mapNotNull null
            MemberProfileResponse(profile = profileResponse, role = member.role)
        }
        return house.toResponse(memberProfileResponses)
    }

    private suspend inline fun <T> ensureMember(
        userId: Long,
        houseId: Long,
        block: () -> HouseDataResult<T>
    ): HouseDataResult<T> {
        return if (houseRepository.isMember(userId, houseId)) {
            block()
        } else {
            HouseDataResult.Error.UserNotAllowed(userId, HouseUserType.REQUEST)
        }
    }
}