package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.diModule
import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.core.dto.EmailInvitationResponse
import com.pollyannawu.justwoo.core.dto.HouseResponse
import com.pollyannawu.justwoo.core.dto.InviteCodeResponse
import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers(disabledWithoutDocker = true)
class HouseInviteFlowTest {

    companion object {
        @Container
        @JvmStatic
        val redis: GenericContainer<*> = GenericContainer("redis:7-alpine").withExposedPorts(6379)
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun testConfig() = MapApplicationConfig(
        "database.host" to "mem",
        "database.port" to "5432",
        "database.dbName" to "justwoo",
        "database.user" to "",
        "database.password" to "",
        "jwt.secret" to "test-secret-that-is-long-enough-32ch",
        "jwt.issuer" to "https://justwoo.test",
        "jwt.audience" to "justwoo-test",
        "jwt.realm" to "test",
        "redis.host" to redis.host,
        "redis.port" to redis.getMappedPort(6379).toString()
    )

    private suspend fun ApplicationTestBuilder.register(email: String): AuthResponse {
        val response = client.post("/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""{"email":"$email","plainPassword":"Password123","deviceId":"test-device"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status, "register $email failed: ${response.bodyAsText()}")
        return json.decodeFromString(response.bodyAsText())
    }

    private suspend fun ApplicationTestBuilder.login(email: String): AuthResponse {
        val response = client.post("/auth/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody("""{"email":"$email","password":"Password123","deviceId":"test-device"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status, "login failed: ${response.bodyAsText()}")
        return json.decodeFromString(response.bodyAsText())
    }

    private suspend fun ApplicationTestBuilder.createHouse(token: String): HouseResponse {
        val now = Clock.System.now()
        val response = client.post("/houses") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"title":"T","name":"TestHouse","adminUserId":0,"memberIds":[],"description":"d","createTime":"$now","updateTime":"$now"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status, "createHouse failed: ${response.bodyAsText()}")
        return json.decodeFromString(response.bodyAsText())
    }

    private suspend fun ApplicationTestBuilder.generateCode(token: String, houseId: Long): InviteCodeResponse {
        val response = client.post("/houses/$houseId/invite-codes") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status, "generateCode failed: ${response.bodyAsText()}")
        return json.decodeFromString(response.bodyAsText())
    }

    private suspend fun ApplicationTestBuilder.submitJoinRequest(token: String, code: String): JoinRequestResponse {
        val response = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"inviteCode":"$code"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status, "submitJoinRequest failed: ${response.bodyAsText()}")
        return json.decodeFromString(response.bodyAsText())
    }

    private suspend fun ApplicationTestBuilder.createEmailInvitation(
        token: String,
        houseId: Long,
        email: String,
    ): EmailInvitationResponse {
        val response = client.post("/houses/$houseId/invitations") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"email":"$email"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status, "createEmailInvitation failed: ${response.bodyAsText()}")
        return json.decodeFromString(response.bodyAsText())
    }

    // ── Task 2.3: House creation tests ────────────────────────────────────────

    @Test
    fun `createHouse success - admin gets assigned`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val token = login(register("admin-create@test.com").user.email).accessToken
        val house = createHouse(token)
        assertTrue(house.id > 0)
        assertTrue(house.members.isNotEmpty())
    }

    @Test
    fun `createHouse fails when user already belongs to a house`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val token = login(register("admin-dup@test.com").user.email).accessToken
        createHouse(token)

        val now = Clock.System.now()
        val response = client.post("/houses") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"title":"T","name":"Second","adminUserId":0,"memberIds":[],"description":"d","createTime":"$now","updateTime":"$now"}""")
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `createHouse fails with empty name`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val token = login(register("admin-emptyname@test.com").user.email).accessToken
        val now = Clock.System.now()
        val response = client.post("/houses") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"title":"T","name":"","adminUserId":0,"memberIds":[],"description":"d","createTime":"$now","updateTime":"$now"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ── Task 3.4: Invite code tests ───────────────────────────────────────────

    @Test
    fun `generateInviteCode success`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val token = login(register("admin-code@test.com").user.email).accessToken
        val house = createHouse(token)

        val code = generateCode(token, house.id)
        assertTrue(code.code.length == 6)
        assertEquals(house.id, code.houseId)
    }

    @Test
    fun `generateInviteCode returns 403 for non-admin`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-403@test.com").user.email).accessToken
        val house = createHouse(adminToken)

        val memberToken = login(register("member-403@test.com").user.email).accessToken

        val response = client.post("/houses/${house.id}/invite-codes") {
            header(HttpHeaders.Authorization, "Bearer $memberToken")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `using an already-used code returns 400`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-used@test.com").user.email).accessToken
        val house = createHouse(adminToken)
        val code = generateCode(adminToken, house.id)

        val member1Token = login(register("mem-used-1@test.com").user.email).accessToken
        submitJoinRequest(member1Token, code.code)

        val member2Token = login(register("mem-used-2@test.com").user.email).accessToken
        val response = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $member2Token")
            setBody("""{"inviteCode":"${code.code}"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `using an invalid code returns 400`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val memberToken = login(register("mem-invalid@test.com").user.email).accessToken
        val response = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $memberToken")
            setBody("""{"inviteCode":"ZZZZZZ"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ── Task 4.5: Join request tests ──────────────────────────────────────────

    @Test
    fun `full join flow - submit request, admin approves, member added`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminAuth = register("admin-flow@test.com")
        val adminToken = login(adminAuth.user.email).accessToken
        val house = createHouse(adminToken)
        val code = generateCode(adminToken, house.id)

        val memberToken = login(register("member-flow@test.com").user.email).accessToken
        val joinRequest = submitJoinRequest(memberToken, code.code)
        assertEquals("PENDING", joinRequest.status.name)

        // admin views pending requests
        val pendingResponse = client.get("/houses/${house.id}/join-requests") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }
        assertEquals(HttpStatusCode.OK, pendingResponse.status)

        // admin approves
        val approveResponse = client.patch("/join-requests/${joinRequest.id}") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            setBody("""{"approve":true}""")
        }
        assertEquals(HttpStatusCode.OK, approveResponse.status)
        val approved = json.decodeFromString<JoinRequestResponse>(approveResponse.bodyAsText())
        assertEquals("APPROVED", approved.status.name)
    }

    @Test
    fun `admin rejects join request`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-reject@test.com").user.email).accessToken
        val house = createHouse(adminToken)
        val code = generateCode(adminToken, house.id)

        val memberToken = login(register("member-reject@test.com").user.email).accessToken
        val joinRequest = submitJoinRequest(memberToken, code.code)

        val response = client.patch("/join-requests/${joinRequest.id}") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            setBody("""{"approve":false}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val rejected = json.decodeFromString<JoinRequestResponse>(response.bodyAsText())
        assertEquals("REJECTED", rejected.status.name)
    }

    @Test
    fun `double-approve returns 409`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-double@test.com").user.email).accessToken
        val house = createHouse(adminToken)
        val code = generateCode(adminToken, house.id)

        val memberToken = login(register("member-double@test.com").user.email).accessToken
        val joinRequest = submitJoinRequest(memberToken, code.code)

        client.patch("/join-requests/${joinRequest.id}") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            setBody("""{"approve":true}""")
        }

        val secondApprove = client.patch("/join-requests/${joinRequest.id}") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            setBody("""{"approve":true}""")
        }
        assertEquals(HttpStatusCode.Conflict, secondApprove.status)
    }

    @Test
    fun `getMyJoinRequestStatus returns latest status`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-status@test.com").user.email).accessToken
        val house = createHouse(adminToken)
        val code = generateCode(adminToken, house.id)

        val memberToken = login(register("member-status@test.com").user.email).accessToken
        submitJoinRequest(memberToken, code.code)

        val response = client.get("/join-requests/me") {
            header(HttpHeaders.Authorization, "Bearer $memberToken")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val status = json.decodeFromString<JoinRequestResponse>(response.bodyAsText())
        assertEquals("PENDING", status.status.name)
    }

    @Test
    fun `user already in house cannot submit join request`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-already@test.com").user.email).accessToken
        val house = createHouse(adminToken)

        val code1 = generateCode(adminToken, house.id)
        val memberToken = login(register("member-already@test.com").user.email).accessToken
        val joinReq = submitJoinRequest(memberToken, code1.code)

        client.patch("/join-requests/${joinReq.id}") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            setBody("""{"approve":true}""")
        }

        val code2 = generateCode(adminToken, house.id)
        val response = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $memberToken")
            setBody("""{"inviteCode":"${code2.code}"}""")
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    // ── invitee-pending-list: GET /invitations/me + auto-approval ────────────

    @Test
    fun `getMyInvitations returns only the caller's active invitations`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-inv-list@test.com").user.email).accessToken
        val house = createHouse(adminToken)
        createEmailInvitation(adminToken, house.id, "invitee-list@test.com")

        register("invitee-list@test.com")
        val inviteeToken = login("invitee-list@test.com").accessToken

        val response = client.get("/invitations/me") {
            header(HttpHeaders.Authorization, "Bearer $inviteeToken")
        }
        assertEquals(HttpStatusCode.OK, response.status, "getMyInvitations failed: ${response.bodyAsText()}")
        val invitations = json.decodeFromString<List<EmailInvitationResponse>>(response.bodyAsText())
        assertEquals(1, invitations.size)
        assertEquals(house.id, invitations.first().houseId)
        assertEquals(house.name, invitations.first().houseName)
    }

    @Test
    fun `getMyInvitations returns empty list for a user with no invitations`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val noInviteToken = login(register("no-invite@test.com").user.email).accessToken

        val response = client.get("/invitations/me") {
            header(HttpHeaders.Authorization, "Bearer $noInviteToken")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val invitations = json.decodeFromString<List<EmailInvitationResponse>>(response.bodyAsText())
        assertTrue(invitations.isEmpty())
    }

    @Test
    fun `submitJoinRequest with correct email invitation code auto-approves and adds member`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-inv-approve@test.com").user.email).accessToken
        val house = createHouse(adminToken)
        val invitation = createEmailInvitation(adminToken, house.id, "invitee-approve@test.com")

        register("invitee-approve@test.com")
        val inviteeToken = login("invitee-approve@test.com").accessToken

        val response = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $inviteeToken")
            setBody("""{"inviteCode":"${invitation.code}"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status, "auto-approve failed: ${response.bodyAsText()}")
        val joinResponse = json.decodeFromString<JoinRequestResponse>(response.bodyAsText())
        assertEquals("APPROVED", joinResponse.status.name)
        assertEquals(house.id, joinResponse.houseId)

        // member is now in the house; submitting any other code fails with Conflict (already a member)
        val secondAttempt = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $inviteeToken")
            setBody("""{"inviteCode":"ANYCODE1"}""")
        }
        assertEquals(HttpStatusCode.Conflict, secondAttempt.status)
    }

    @Test
    fun `submitJoinRequest with incorrect code returns 400`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val inviteeToken = login(register("invitee-wrong-code@test.com").user.email).accessToken

        val response = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $inviteeToken")
            setBody("""{"inviteCode":"NOPE0000"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `submitJoinRequest with already-used email invitation code returns 400`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-inv-used@test.com").user.email).accessToken
        val house = createHouse(adminToken)
        val invitation = createEmailInvitation(adminToken, house.id, "invitee-used@test.com")

        register("invitee-used@test.com")
        val inviteeToken = login("invitee-used@test.com").accessToken

        // first redemption succeeds and marks the invitation used
        val firstResponse = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $inviteeToken")
            setBody("""{"inviteCode":"${invitation.code}"}""")
        }
        assertEquals(HttpStatusCode.OK, firstResponse.status)

        // a second user tries to redeem the same already-used code
        val otherToken = login(register("invitee-used-2@test.com").user.email).accessToken
        val secondResponse = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $otherToken")
            setBody("""{"inviteCode":"${invitation.code}"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, secondResponse.status)
    }

    @Test
    fun `submitJoinRequest with email invitation code addressed to a different email returns 400`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val adminToken = login(register("admin-inv-wrong-email@test.com").user.email).accessToken
        val house = createHouse(adminToken)
        val invitation = createEmailInvitation(adminToken, house.id, "intended-invitee@test.com")

        // a different user tries to redeem a code that was scoped to someone else's email
        val otherToken = login(register("not-the-invitee@test.com").user.email).accessToken
        val response = client.post("/join-requests") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $otherToken")
            setBody("""{"inviteCode":"${invitation.code}"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
