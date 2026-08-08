package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.EmailInvitationRepository
import com.pollyannawu.justwoo.backend.repositories.InviteCodeRepository
import com.pollyannawu.justwoo.backend.repositories.JoinRequestRepository
import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.auth.AuthRepository
import com.pollyannawu.justwoo.backend.schema.EmailInvitationRow
import com.pollyannawu.justwoo.backend.utils.dataresult.HouseDataResult
import com.pollyannawu.justwoo.backend.utils.mapper.User
import com.pollyannawu.justwoo.core.House
import com.pollyannawu.justwoo.core.JoinRequestStatus
import com.pollyannawu.justwoo.core.MemberRole
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class HouseInviteServiceTest {

    private val houseRepo: HouseRepository = mockk()
    private val inviteCodeRepo: InviteCodeRepository = mockk()
    private val joinRequestRepo: JoinRequestRepository = mockk()
    private val emailInvitationRepo: EmailInvitationRepository = mockk()
    private val authRepo: AuthRepository = mockk()

    private val service: HouseInviteService = DefaultHouseInviteService(
        houseRepo, inviteCodeRepo, joinRequestRepo, emailInvitationRepo, authRepo
    )

    private val now = Clock.System.now()
    private val adminId = 1L
    private val houseId = 10L

    private val fakeHouse = House(
        id = houseId,
        name = "Test House",
        avatar = "avatar.png",
        description = "desc",
        members = emptyList(),
        createTime = now,
        updateTime = now,
    )

    private fun fakeInvitationRow(
        email: String,
        code: String = "ABCD1234",
        expiresAt: Instant = now + 7.days,
    ) = EmailInvitationRow(
        id = 1L,
        houseId = houseId,
        inviteeEmail = email,
        code = code,
        expiresAt = expiresAt,
        used = false,
    )

    @BeforeEach
    fun setup() {
        coEvery { houseRepo.isAdmin(adminId, houseId) } returns true
        coEvery { houseRepo.findById(houseId) } returns fakeHouse
        coEvery { emailInvitationRepo.invalidateExisting(any(), any()) } returns Unit
    }

    @Test
    fun `createEmailInvitation returns code on success`() = runTest {
        val email = "friend@example.com"
        coEvery { emailInvitationRepo.create(houseId, email, any(), any()) } returns fakeInvitationRow(email)

        val result = service.createEmailInvitation(adminId, houseId, email)

        assertInstanceOf(HouseDataResult.Success::class.java, result)
        val data = (result as HouseDataResult.Success).data
        assertEquals(houseId, data.houseId)
        assertEquals("Test House", data.houseName)
        assertEquals("ABCD1234", data.code)
    }

    @Test
    fun `createEmailInvitation normalises email to lowercase`() = runTest {
        val rawEmail = "Friend@Example.COM"
        val normalised = "friend@example.com"
        coEvery { emailInvitationRepo.create(houseId, normalised, any(), any()) } returns fakeInvitationRow(normalised)

        service.createEmailInvitation(adminId, houseId, rawEmail)

        coVerify { emailInvitationRepo.create(houseId, normalised, any(), any()) }
        coVerify { emailInvitationRepo.invalidateExisting(houseId, normalised) }
    }

    @Test
    fun `createEmailInvitation returns Forbidden when caller is not admin`() = runTest {
        coEvery { houseRepo.isAdmin(adminId, houseId) } returns false

        val result = service.createEmailInvitation(adminId, houseId, "a@b.com")

        assertInstanceOf(HouseDataResult.Error.UserNotAllowed::class.java, result)
    }

    @Test
    fun `createEmailInvitation returns HouseNotFound when house does not exist`() = runTest {
        coEvery { houseRepo.findById(houseId) } returns null

        val result = service.createEmailInvitation(adminId, houseId, "a@b.com")

        assertInstanceOf(HouseDataResult.Error.HouseNotFound::class.java, result)
    }

    // ── getMyInvitations ────────────────────────────────────────────────

    private val inviteeId = 42L
    private val inviteeEmail = "invitee@example.com"
    private val inviteeUser = User(
        id = inviteeId,
        email = inviteeEmail,
        passwordHash = "hash",
        userRefreshTokenId = 0L,
        createTime = now,
        updateTime = now,
    )

    @Test
    fun `getMyInvitations returns invitations for caller's email with house details`() = runTest {
        coEvery { authRepo.getUser(inviteeId.toString()) } returns inviteeUser
        coEvery { emailInvitationRepo.findActiveByEmail(inviteeEmail) } returns listOf(fakeInvitationRow(inviteeEmail))
        coEvery { houseRepo.findById(houseId) } returns fakeHouse

        val result = service.getMyInvitations(inviteeId)

        assertInstanceOf(HouseDataResult.Success::class.java, result)
        val data = (result as HouseDataResult.Success).data
        assertEquals(1, data.size)
        assertEquals("Test House", data.first().houseName)
        assertEquals(houseId, data.first().houseId)
    }

    @Test
    fun `getMyInvitations returns empty list when caller has no active invitations`() = runTest {
        coEvery { authRepo.getUser(inviteeId.toString()) } returns inviteeUser
        coEvery { emailInvitationRepo.findActiveByEmail(inviteeEmail) } returns emptyList()

        val result = service.getMyInvitations(inviteeId)

        assertInstanceOf(HouseDataResult.Success::class.java, result)
        assertEquals(emptyList<Any>(), (result as HouseDataResult.Success).data)
    }

    // ── submitJoinRequest: email invitation auto-approval ───────────────

    @Test
    fun `submitJoinRequest auto-approves when code matches an active email invitation`() = runTest {
        val emailCode = "ABCD1234"
        coEvery { houseRepo.isAnyMember(inviteeId) } returns false
        coEvery { authRepo.getUser(inviteeId.toString()) } returns inviteeUser
        coEvery { emailInvitationRepo.findByCode(emailCode) } returns fakeInvitationRow(inviteeEmail, code = emailCode)
        coEvery { emailInvitationRepo.markUsed(1L) } returns true
        coEvery { houseRepo.addMember(inviteeId, MemberRole.MEMBER, houseId, any()) } returns fakeHouse

        val result = service.submitJoinRequest(inviteeId, emailCode)

        assertInstanceOf(HouseDataResult.Success::class.java, result)
        val data = (result as HouseDataResult.Success).data
        assertEquals(JoinRequestStatus.APPROVED, data.status)
        coVerify { emailInvitationRepo.markUsed(1L) }
        coVerify { houseRepo.addMember(inviteeId, MemberRole.MEMBER, houseId, any()) }
    }

    @Test
    fun `submitJoinRequest falls through to generic code path when no email invitation matches`() = runTest {
        val genericCode = "GEN123"
        coEvery { houseRepo.isAnyMember(inviteeId) } returns false
        coEvery { authRepo.getUser(inviteeId.toString()) } returns inviteeUser
        coEvery { emailInvitationRepo.findByCode(genericCode) } returns null
        coEvery { inviteCodeRepo.findByCode(genericCode) } returns null

        val result = service.submitJoinRequest(inviteeId, genericCode)

        assertInstanceOf(HouseDataResult.Error.InvalidCode::class.java, result)
    }

    @Test
    fun `submitJoinRequest returns InvalidCode when email invitation is expired`() = runTest {
        val emailCode = "EXPIRED1"
        coEvery { houseRepo.isAnyMember(inviteeId) } returns false
        coEvery { authRepo.getUser(inviteeId.toString()) } returns inviteeUser
        coEvery { emailInvitationRepo.findByCode(emailCode) } returns fakeInvitationRow(
            inviteeEmail, code = emailCode, expiresAt = now - 1.days
        )
        coEvery { inviteCodeRepo.findByCode(emailCode) } returns null

        val result = service.submitJoinRequest(inviteeId, emailCode)

        assertInstanceOf(HouseDataResult.Error.InvalidCode::class.java, result)
    }

    @Test
    fun `submitJoinRequest returns InvalidCode when email invitation already used`() = runTest {
        val emailCode = "USED1234"
        coEvery { houseRepo.isAnyMember(inviteeId) } returns false
        coEvery { authRepo.getUser(inviteeId.toString()) } returns inviteeUser
        coEvery { emailInvitationRepo.findByCode(emailCode) } returns fakeInvitationRow(
            inviteeEmail, code = emailCode
        ).copy(used = true)
        coEvery { inviteCodeRepo.findByCode(emailCode) } returns null

        val result = service.submitJoinRequest(inviteeId, emailCode)

        assertInstanceOf(HouseDataResult.Error.InvalidCode::class.java, result)
    }

    @Test
    fun `submitJoinRequest returns InvalidCode when email invitation matches different user's email`() = runTest {
        val emailCode = "OTHER123"
        coEvery { houseRepo.isAnyMember(inviteeId) } returns false
        coEvery { authRepo.getUser(inviteeId.toString()) } returns inviteeUser
        coEvery { emailInvitationRepo.findByCode(emailCode) } returns fakeInvitationRow(
            "someone-else@example.com", code = emailCode
        )
        coEvery { inviteCodeRepo.findByCode(emailCode) } returns null

        val result = service.submitJoinRequest(inviteeId, emailCode)

        assertInstanceOf(HouseDataResult.Error.InvalidCode::class.java, result)
    }
}
