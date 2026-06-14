package com.pollyannawu.justwoo.data.network.service

import com.pollyannawu.justwoo.core.dto.InviteCodeResponse
import com.pollyannawu.justwoo.core.dto.JoinRequestBody
import com.pollyannawu.justwoo.core.dto.JoinRequestDecision
import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import com.pollyannawu.justwoo.data.network.safeApiCall
import com.pollyannawu.justwoo.model.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

interface HouseInviteApiService {
    suspend fun generateInviteCode(houseId: Long): ApiResult<InviteCodeResponse>
    suspend fun submitJoinRequest(inviteCode: String): ApiResult<JoinRequestResponse>
    suspend fun getPendingRequests(houseId: Long): ApiResult<List<JoinRequestResponse>>
    suspend fun processJoinRequest(requestId: Long, approve: Boolean): ApiResult<JoinRequestResponse>
    suspend fun getMyJoinRequestStatus(): ApiResult<JoinRequestResponse>
}

class DefaultHouseInviteApiService(
    private val ktorClient: HttpClient,
) : HouseInviteApiService {

    override suspend fun generateInviteCode(houseId: Long): ApiResult<InviteCodeResponse> =
        safeApiCall(tag = "HouseInviteApi.generateInviteCode") {
            ktorClient.post("/houses/$houseId/invite-codes").body()
        }

    override suspend fun submitJoinRequest(inviteCode: String): ApiResult<JoinRequestResponse> =
        safeApiCall(tag = "HouseInviteApi.submitJoinRequest") {
            ktorClient.post("/join-requests") {
                setBody(JoinRequestBody(inviteCode = inviteCode))
            }.body()
        }

    override suspend fun getPendingRequests(houseId: Long): ApiResult<List<JoinRequestResponse>> =
        safeApiCall(tag = "HouseInviteApi.getPendingRequests") {
            ktorClient.get("/houses/$houseId/join-requests").body()
        }

    override suspend fun processJoinRequest(requestId: Long, approve: Boolean): ApiResult<JoinRequestResponse> =
        safeApiCall(tag = "HouseInviteApi.processJoinRequest") {
            ktorClient.patch("/join-requests/$requestId") {
                setBody(JoinRequestDecision(approve = approve))
            }.body()
        }

    override suspend fun getMyJoinRequestStatus(): ApiResult<JoinRequestResponse> =
        safeApiCall(tag = "HouseInviteApi.getMyJoinRequestStatus") {
            ktorClient.get("/join-requests/me").body()
        }
}
