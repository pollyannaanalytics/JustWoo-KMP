package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.EmailInvitationRepository
import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.InviteCodeRepository
import com.pollyannawu.justwoo.backend.repositories.JoinRequestRepository
import com.pollyannawu.justwoo.backend.repositories.auth.AuthRepository
import com.pollyannawu.justwoo.backend.schema.EmailInvitationRow
import com.pollyannawu.justwoo.backend.utils.dataresult.HouseDataResult
import com.pollyannawu.justwoo.backend.utils.dataresult.HouseUserType
import com.pollyannawu.justwoo.core.JoinRequestStatus
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.dto.EmailInvitationResponse
import com.pollyannawu.justwoo.core.dto.InviteCodeResponse
import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import com.pollyannawu.justwoo.core.dto.OtpInviteSession
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import redis.clients.jedis.JedisPool
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

interface HouseInviteService {
    suspend fun generateInviteCode(adminId: Long, houseId: Long): HouseDataResult<InviteCodeResponse>
    suspend fun createEmailInvitation(adminId: Long, houseId: Long, email: String): HouseDataResult<EmailInvitationResponse>
    suspend fun submitJoinRequest(userId: Long, inviteCode: String): HouseDataResult<JoinRequestResponse>
    suspend fun getPendingRequests(adminId: Long, houseId: Long): HouseDataResult<List<JoinRequestResponse>>
    suspend fun processJoinRequest(adminId: Long, requestId: Long, approve: Boolean): HouseDataResult<JoinRequestResponse>
    suspend fun getMyJoinRequestStatus(userId: Long): HouseDataResult<JoinRequestResponse>
    suspend fun getMyInvitations(userId: Long): HouseDataResult<List<EmailInvitationResponse>>
    suspend fun generateOtpSession(userId: String): HouseDataResult<OtpInviteSession>
    suspend fun confirmOtpInvite(code: String, displayNumber: Int, houseId: Long, requestingUserId: Long): HouseDataResult<Unit>
}

class DefaultHouseInviteService(
    private val houseRepository: HouseRepository,
    private val inviteCodeRepository: InviteCodeRepository,
    private val joinRequestRepository: JoinRequestRepository,
    private val emailInvitationRepository: EmailInvitationRepository,
    private val authRepository: AuthRepository,
    private val jedisPool: JedisPool,
) : HouseInviteService {

    private val secureRandom = SecureRandom()

    private fun otpKey(code: String) = "otp:invite:$code"
    private fun otpUserKey(userId: String) = "otp:invite:user:$userId"

    override suspend fun generateOtpSession(userId: String): HouseDataResult<OtpInviteSession> {
        jedisPool.resource.use { jedis ->
            // Return existing session if one is still active
            val existingCode = jedis.get(otpUserKey(userId))
            if (existingCode != null) {
                val remaining = jedis.ttl(otpUserKey(userId))
                val sessionJson = jedis.get(otpKey(existingCode))
                if (sessionJson != null && remaining > 0) {
                    val obj = Json.parseToJsonElement(sessionJson).jsonObject
                    val displayNumber = obj["displayNumber"]!!.jsonPrimitive.content.toInt()
                    return HouseDataResult.Success(OtpInviteSession(existingCode, displayNumber, remaining.toInt()))
                }
            }

            // No active session — generate a new one
            val code = (100_000 + secureRandom.nextInt(900_000)).toString()
            val displayNumber = 10 + secureRandom.nextInt(90)
            val ttlSeconds = 60L

            val value = buildJsonObject {
                put("userId", userId)
                put("displayNumber", displayNumber)
            }.toString()

            jedis.setex(otpKey(code), ttlSeconds, value)
            jedis.setex(otpUserKey(userId), ttlSeconds, code)

            return HouseDataResult.Success(OtpInviteSession(code, displayNumber, ttlSeconds.toInt()))
        }
    }

    override suspend fun confirmOtpInvite(
        code: String,
        displayNumber: Int,
        houseId: Long,
        requestingUserId: Long,
    ): HouseDataResult<Unit> {
        if (!houseRepository.isAdmin(requestingUserId, houseId)) {
            return HouseDataResult.Error.UserNotAllowed(requestingUserId, HouseUserType.ADMIN)
        }

        jedisPool.resource.use { jedis ->
            val sessionJson = jedis.get(otpKey(code))
                ?: return HouseDataResult.Error.NotFound

            val obj = Json.parseToJsonElement(sessionJson).jsonObject
            val storedDisplay = obj["displayNumber"]!!.jsonPrimitive.content.toInt()
            val storedUserId = obj["userId"]!!.jsonPrimitive.content

            if (displayNumber != storedDisplay) {
                return HouseDataResult.Error.ValidationError("Code and number do not match")
            }

            val memberId = storedUserId.toLong()
            houseRepository.addMember(
                userId = memberId,
                memberRole = MemberRole.MEMBER,
                houseId = houseId,
                joinedAt = Clock.System.now(),
            )

            jedis.del(otpKey(code))
            jedis.del(otpUserKey(storedUserId))

            return HouseDataResult.Success(Unit)
        }
    }

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

    override suspend fun createEmailInvitation(
        adminId: Long,
        houseId: Long,
        email: String,
    ): HouseDataResult<EmailInvitationResponse> {
        if (!houseRepository.isAdmin(adminId, houseId)) {
            return HouseDataResult.Error.UserNotAllowed(adminId, HouseUserType.ADMIN)
        }
        val house = houseRepository.findById(houseId)
            ?: return HouseDataResult.Error.HouseNotFound

        val normalizedEmail = email.lowercase().trim()
        emailInvitationRepository.invalidateExisting(houseId, normalizedEmail)

        val code = generateEmailInviteCode()
        val expiresAt = Clock.System.now() + 7.days
        val row = emailInvitationRepository.create(houseId, normalizedEmail, code, expiresAt)

        return HouseDataResult.Success(
            EmailInvitationResponse(
                id = row.id,
                houseId = row.houseId,
                houseName = house.name,
                houseAvatar = house.avatar,
                code = row.code,
                expiresAt = row.expiresAt,
            )
        )
    }

    override suspend fun submitJoinRequest(userId: Long, inviteCode: String): HouseDataResult<JoinRequestResponse> {
        if (houseRepository.isAnyMember(userId)) {
            return HouseDataResult.Error.AlreadyMember
        }

        val emailInvitationResult = tryAutoApproveEmailInvitation(userId, inviteCode)
        if (emailInvitationResult != null) {
            return emailInvitationResult
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

    /**
     * Checks whether [inviteCode] matches an `email_invitations` row.
     *
     * Returns:
     * - `null` if the code does not match any email invitation at all (caller should
     *   fall through to the generic invite-code path unchanged).
     * - [HouseDataResult.Error.InvalidCode] if the code matches an email invitation row but
     *   it is expired, already used, or not addressed to the caller's email (we don't leak
     *   which condition to keep the InviteCodeRepository path indistinguishable for an
     *   attacker probing codes).
     * - [HouseDataResult.Success] with an auto-approved [JoinRequestResponse] when the code
     *   matches an active invitation for the caller's email — the user is added as MEMBER
     *   immediately and the invitation is marked used.
     */
    private suspend fun tryAutoApproveEmailInvitation(
        userId: Long,
        inviteCode: String,
    ): HouseDataResult<JoinRequestResponse>? {
        val invitationRow = emailInvitationRepository.findByCode(inviteCode) ?: return null

        val callerEmail = authRepository.getUser(userId.toString()).email.lowercase().trim()
        val now = Clock.System.now()
        val isValid = !invitationRow.used &&
            invitationRow.expiresAt > now &&
            invitationRow.inviteeEmail == callerEmail

        if (!isValid) {
            return HouseDataResult.Error.InvalidCode
        }

        emailInvitationRepository.markUsed(invitationRow.id)
        houseRepository.addMember(
            userId = userId,
            memberRole = MemberRole.MEMBER,
            houseId = invitationRow.houseId,
            joinedAt = now,
        )

        return HouseDataResult.Success(
            JoinRequestResponse(
                id = invitationRow.id,
                houseId = invitationRow.houseId,
                userId = userId,
                status = JoinRequestStatus.APPROVED,
                createdAt = now,
            )
        )
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

    override suspend fun getMyInvitations(userId: Long): HouseDataResult<List<EmailInvitationResponse>> {
        val email = authRepository.getUser(userId.toString()).email.lowercase().trim()
        val rows = emailInvitationRepository.findActiveByEmail(email)
        val responses = rows.mapNotNull { row -> row.toResponseOrNull() }
        return HouseDataResult.Success(responses)
    }

    private suspend fun EmailInvitationRow.toResponseOrNull(): EmailInvitationResponse? {
        val house = houseRepository.findById(houseId) ?: return null
        return EmailInvitationResponse(
            id = id,
            houseId = houseId,
            houseName = house.name,
            houseAvatar = house.avatar,
            code = code,
            expiresAt = expiresAt,
        )
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    private fun generateEmailInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}

private fun com.pollyannawu.justwoo.backend.schema.JoinRequestRow.toResponse() = JoinRequestResponse(
    id = id,
    houseId = houseId,
    userId = userId,
    status = status,
    createdAt = createdAt,
)
