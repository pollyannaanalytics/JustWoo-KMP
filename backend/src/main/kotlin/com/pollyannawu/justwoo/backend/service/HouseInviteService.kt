package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.InviteCodeRepository
import com.pollyannawu.justwoo.backend.repositories.JoinRequestRepository
import com.pollyannawu.justwoo.backend.utils.dataresult.HouseDataResult
import com.pollyannawu.justwoo.backend.utils.dataresult.HouseUserType
import com.pollyannawu.justwoo.core.JoinRequestStatus
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.dto.InviteCodeResponse
import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

interface HouseInviteService {
    suspend fun generateInviteCode(adminId: Long, houseId: Long): HouseDataResult<InviteCodeResponse>
    suspend fun submitJoinRequest(userId: Long, inviteCode: String): HouseDataResult<JoinRequestResponse>
    suspend fun getPendingRequests(adminId: Long, houseId: Long): HouseDataResult<List<JoinRequestResponse>>
    suspend fun processJoinRequest(adminId: Long, requestId: Long, approve: Boolean): HouseDataResult<JoinRequestResponse>
    suspend fun getMyJoinRequestStatus(userId: Long): HouseDataResult<JoinRequestResponse>
}

class DefaultHouseInviteService(
    private val houseRepository: HouseRepository,
    private val inviteCodeRepository: InviteCodeRepository,
    private val joinRequestRepository: JoinRequestRepository,
) : HouseInviteService {

    override suspend fun generateInviteCode(adminId: Long, houseId: Long): HouseDataResult<InviteCodeResponse> {
        if (!houseRepository.isAdmin(adminId, houseId)) {
            return HouseDataResult.Error.UserNotAllowed(adminId, HouseUserType.ADMIN)
        }
        val code = generateCode()
        val expiresAt = Clock.System.now() + 15.minutes
        val row = inviteCodeRepository.create(houseId, code, expiresAt, adminId)
        return HouseDataResult.Success(
            InviteCodeResponse(code = row.code, expiresAt = row.expiresAt, houseId = row.houseId)
        )
    }

    override suspend fun submitJoinRequest(userId: Long, inviteCode: String): HouseDataResult<JoinRequestResponse> {
        if (houseRepository.isAnyMember(userId)) {
            return HouseDataResult.Error.AlreadyMember
        }
        val codeRow = inviteCodeRepository.findByCode(inviteCode)
            ?: return HouseDataResult.Error.InvalidCode

        val now = Clock.System.now()
        if (codeRow.used || codeRow.expiresAt < now) {
            return HouseDataResult.Error.InvalidCode
        }

        inviteCodeRepository.markUsed(codeRow.id)

        val request = joinRequestRepository.create(
            houseId = codeRow.houseId,
            userId = userId,
            inviteCodeId = codeRow.id,
        )
        return HouseDataResult.Success(request.toResponse())
    }

    override suspend fun getPendingRequests(adminId: Long, houseId: Long): HouseDataResult<List<JoinRequestResponse>> {
        if (!houseRepository.isAdmin(adminId, houseId)) {
            return HouseDataResult.Error.UserNotAllowed(adminId, HouseUserType.ADMIN)
        }
        val rows = joinRequestRepository.findPending(houseId)
        return HouseDataResult.Success(rows.map { it.toResponse() })
    }

    override suspend fun processJoinRequest(
        adminId: Long,
        requestId: Long,
        approve: Boolean,
    ): HouseDataResult<JoinRequestResponse> {
        val request = joinRequestRepository.findById(requestId)
            ?: return HouseDataResult.Error.NotFound

        if (!houseRepository.isAdmin(adminId, request.houseId)) {
            return HouseDataResult.Error.UserNotAllowed(adminId, HouseUserType.ADMIN)
        }

        val newStatus = if (approve) JoinRequestStatus.APPROVED else JoinRequestStatus.REJECTED
        val updated = joinRequestRepository.updateStatus(requestId, newStatus)
        if (!updated) {
            return HouseDataResult.Error.AlreadyProcessed
        }

        if (approve) {
            houseRepository.addMember(
                userId = request.userId,
                memberRole = MemberRole.MEMBER,
                houseId = request.houseId,
                joinedAt = Clock.System.now(),
            )
        }

        return joinRequestRepository.findById(requestId)?.let {
            HouseDataResult.Success(it.toResponse())
        } ?: HouseDataResult.Error.NotFound
    }

    override suspend fun getMyJoinRequestStatus(userId: Long): HouseDataResult<JoinRequestResponse> {
        val row = joinRequestRepository.findLatestByUser(userId)
            ?: return HouseDataResult.Error.NotFound
        return HouseDataResult.Success(row.toResponse())
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}

private fun com.pollyannawu.justwoo.backend.schema.JoinRequestRow.toResponse() = JoinRequestResponse(
    id = id,
    houseId = houseId,
    userId = userId,
    status = status,
    createdAt = createdAt,
)
