package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.JoinRequestStatus
import com.pollyannawu.justwoo.core.dto.EmailInvitationResponse
import com.pollyannawu.justwoo.core.dto.InviteCodeResponse
import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import com.pollyannawu.justwoo.core.dto.OtpConfirmRequest
import com.pollyannawu.justwoo.core.dto.OtpInviteSession
import com.pollyannawu.justwoo.data.HouseInviteRepository
import kotlinx.datetime.Instant

internal fun stubEmailInvitationResponse(id: Long = 1L) = EmailInvitationResponse(
    id = id,
    houseId = 10L,
    houseName = "Test House",
    houseAvatar = "",
    code = "ABC123",
    expiresAt = Instant.fromEpochMilliseconds(0),
)

internal fun stubJoinRequestResponse(
    id: Long = 1L,
    status: JoinRequestStatus = JoinRequestStatus.APPROVED,
) = JoinRequestResponse(
    id = id,
    houseId = 10L,
    userId = 99L,
    status = status,
    createdAt = Instant.fromEpochMilliseconds(0),
)

internal class FakeHouseInviteRepository(
    var myEmailInvitations: List<EmailInvitationResponse> = emptyList(),
    var myEmailInvitationsError: Throwable? = null,
    var submitJoinRequestResult: JoinRequestResponse = stubJoinRequestResponse(),
    var submitJoinRequestError: Throwable? = null,
    var createEmailInvitationResult: EmailInvitationResponse = stubEmailInvitationResponse(),
    var createEmailInvitationError: Throwable? = null,
    var generateOtpSessionResult: OtpInviteSession? = null,
    var generateOtpSessionError: Throwable? = null,
    var confirmOtpInviteError: Throwable? = null,
) : HouseInviteRepository {

    val submittedCodes = mutableListOf<String>()
    var getMyEmailInvitationsCallCount = 0
    val createdInvitations = mutableListOf<Pair<Long, String>>()

    override suspend fun generateInviteCode(houseId: Long): InviteCodeResponse =
        throw UnsupportedOperationException()

    override suspend fun createEmailInvitation(houseId: Long, email: String): EmailInvitationResponse {
        createdInvitations += houseId to email
        createEmailInvitationError?.let { throw it }
        return createEmailInvitationResult
    }

    override suspend fun createHouse(name: String, description: String) =
        throw UnsupportedOperationException()

    override suspend fun submitJoinRequest(inviteCode: String): JoinRequestResponse {
        submittedCodes += inviteCode
        submitJoinRequestError?.let { throw it }
        return submitJoinRequestResult
    }

    override suspend fun getPendingRequests(houseId: Long): List<JoinRequestResponse> =
        throw UnsupportedOperationException()

    override suspend fun approveRequest(requestId: Long): JoinRequestResponse =
        throw UnsupportedOperationException()

    override suspend fun rejectRequest(requestId: Long): JoinRequestResponse =
        throw UnsupportedOperationException()

    override suspend fun getMyJoinRequestStatus(): JoinRequestResponse? = null

    override suspend fun getMyEmailInvitations(): List<EmailInvitationResponse> {
        getMyEmailInvitationsCallCount++
        myEmailInvitationsError?.let { throw it }
        return myEmailInvitations
    }

    override suspend fun generateOtpSession(): OtpInviteSession {
        generateOtpSessionError?.let { throw it }
        return generateOtpSessionResult ?: error("generateOtpSessionResult not set")
    }

    override suspend fun confirmOtpInvite(request: OtpConfirmRequest) {
        confirmOtpInviteError?.let { throw it }
    }
}
