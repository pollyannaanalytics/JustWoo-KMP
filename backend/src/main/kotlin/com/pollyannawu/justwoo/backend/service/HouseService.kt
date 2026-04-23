package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.database.utils.PagedResult
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
import com.pollyannawu.justwoo.core.dto.PageResponse
import kotlinx.datetime.Clock
import kotlin.collections.mapNotNull
import kotlin.math.ceil

interface HouseService {
    suspend fun createHouse(request: HouseRequest): HouseDataResult<HouseResponse>
    suspend fun addMember(requesterId: Long, userId: Long, houseId: Long): HouseDataResult<HouseResponse>
    suspend fun removeMember(requesterId: Long, userId: Long, houseId: Long): HouseDataResult<HouseResponse>
    suspend fun updateHouseContent(
        userId: Long,
        houseId: Long,
        house: HouseRequest
    ): HouseDataResult<HouseResponse>

    suspend fun getHouses(userId: Long, page: Int): HouseDataResult<PageResponse<HouseResponse>>
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
            val currentMembers = houseRepository.getHouseMembers(houseId)
            if (!currentMembers.any { it.userId == userId }) {
                val now = Clock.System.now()
                val house = houseRepository.addMember(userId, MemberRole.MEMBER, houseId, now)
                return HouseDataResult.Success(
                    mergeHouseAndProfileDetails(house)
                )
            } else {
                val house = houseRepository.getPagedHouses(requesterId, houseId, 1, 0).items.first()
                HouseDataResult.Success(
                    mergeHouseAndProfileDetails(house)
                )
            }

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

    override suspend fun getHouses(userId: Long, page: Int): HouseDataResult<PageResponse<HouseResponse>> {
        val offset = (page - 1).toLong() * HOUSE_PAGE_SIZE


        val pagedHouses = getPagedHouses(userId = userId, offset = offset)
        val totalCount = pagedHouses.totalCount
        val totalPages = ceil(totalCount.toDouble() / HOUSE_PAGE_SIZE).toInt()
        val isLast = (page.toLong() * HOUSE_PAGE_SIZE) >= totalCount

        val memberId = pagedHouses.items.flatMap { it.members }.map { it.userId }
        val profiles = profileRepository.getProfiles(memberId).associateBy { it.id }

        val houseResponses = pagedHouses.items.map { house ->
            val memberResponses = house.members.mapNotNull { member ->
                val profile = profiles[member.userId] ?: return@mapNotNull null
                MemberProfileResponse(
                    profile = profile.toResponse(),
                    role = member.role,
                    joinAt = member.joinedAt
                )
            }
            house.toResponse(memberResponses)
        }

        return HouseDataResult.Success(
            PageResponse(
                content = houseResponses,
                pageNumber = page,
                pageSize = HOUSE_PAGE_SIZE,
                totalElements = totalCount,
                totalPages = totalPages,
                isLast = isLast
            ))
    }

    override suspend fun getHouse(
        userId: Long,
        houseId: Long
    ): HouseDataResult<HouseResponse> = ensureMember(userId, houseId) {
        return getPagedHouses(userId, houseId, offset = 0).takeIf { it.items.isNotEmpty() }?.let {
            val house = it.items.first()
            return HouseDataResult.Success(mergeHouseAndProfileDetails(house))
        } ?: run {
            HouseDataResult.Error.HouseNotFound
        }
    }

    private suspend fun getPagedHouses(userId: Long, houseId: Long? = null, offset: Long): PagedResult<House> {
        return houseRepository.getPagedHouses(userId, houseId, HOUSE_PAGE_SIZE, offset)
    }

    private suspend fun mergeHouseAndProfileDetails(
        house: House
    ): HouseResponse {
        val memberIds = house.members.map { it.userId }.distinct()

        val members = house.members
        val profiles = profileRepository.getProfiles(memberIds).associateBy { it.id }

        val memberProfileResponses = members.mapNotNull { member ->
            val profileResponse = profiles[member.userId]?.toResponse() ?: return@mapNotNull null
            MemberProfileResponse(profile = profileResponse, role = member.role, joinAt = member.joinedAt)
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
    companion object{
        private const val HOUSE_PAGE_SIZE = 20
    }
}