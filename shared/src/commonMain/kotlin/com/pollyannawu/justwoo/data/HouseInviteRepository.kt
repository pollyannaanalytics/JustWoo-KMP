package com.pollyannawu.justwoo.data

import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.dto.InviteCodeResponse
import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import com.pollyannawu.justwoo.data.network.service.HouseInviteApiService
import com.pollyannawu.justwoo.model.ApiResult
import kotlinx.datetime.Clock

interface HouseInviteRepository {
    suspend fun generateInviteCode(houseId: Long): InviteCodeResponse
    suspend fun createHouse(name: String, description: String)
    suspend fun submitJoinRequest(inviteCode: String): JoinRequestResponse
    suspend fun getPendingRequests(houseId: Long): List<JoinRequestResponse>
    suspend fun approveRequest(requestId: Long): JoinRequestResponse
    suspend fun rejectRequest(requestId: Long): JoinRequestResponse
    suspend fun getMyJoinRequestStatus(): JoinRequestResponse?
}

class DefaultHouseInviteRepository(
    private val houseInviteApiService: HouseInviteApiService,
    private val houseRepository: HouseRepository,
) : HouseInviteRepository {

    override suspend fun generateInviteCode(houseId: Long): InviteCodeResponse {
        val result = houseInviteApiService.generateInviteCode(houseId)
        if (result is ApiResult.Success) return result.data
        if (result is ApiResult.Error) throw Exception(result.exception)
        error("Unexpected state")
    }

    override suspend fun createHouse(name: String, description: String) {
        val now = Clock.System.now()
        val house = House(
            id = 0,
            name = name,
            description = description,
            avatar = "",
            members = emptyList(),
            createTime = now,
            updateTime = now,
        )
        houseRepository.createHouse(house)
    }

    override suspend fun submitJoinRequest(inviteCode: String): JoinRequestResponse {
        val result = houseInviteApiService.submitJoinRequest(inviteCode)
        if (result is ApiResult.Success) return result.data
        if (result is ApiResult.Error) throw Exception(result.exception)
        error("Unexpected state")
    }

    override suspend fun getPendingRequests(houseId: Long): List<JoinRequestResponse> {
        val result = houseInviteApiService.getPendingRequests(houseId)
        if (result is ApiResult.Success) return result.data
        if (result is ApiResult.Error) throw Exception(result.exception)
        error("Unexpected state")
    }

    override suspend fun approveRequest(requestId: Long): JoinRequestResponse {
        val result = houseInviteApiService.processJoinRequest(requestId, approve = true)
        if (result is ApiResult.Success) return result.data
        if (result is ApiResult.Error) throw Exception(result.exception)
        error("Unexpected state")
    }

    override suspend fun rejectRequest(requestId: Long): JoinRequestResponse {
        val result = houseInviteApiService.processJoinRequest(requestId, approve = false)
        if (result is ApiResult.Success) return result.data
        if (result is ApiResult.Error) throw Exception(result.exception)
        error("Unexpected state")
    }

    override suspend fun getMyJoinRequestStatus(): JoinRequestResponse? {
        val result = houseInviteApiService.getMyJoinRequestStatus()
        if (result is ApiResult.Success) return result.data
        return null
    }
}
