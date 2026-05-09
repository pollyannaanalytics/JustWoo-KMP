package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.diModule
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.dto.AuthResponse
import com.pollyannawu.justwoo.core.dto.HouseResponse
import com.pollyannawu.justwoo.core.dto.SettlementResponse
import com.pollyannawu.justwoo.core.dto.TaskResponse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers(disabledWithoutDocker = true)
class UserFlowIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val redis: GenericContainer<*> = GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)
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

    // ── helpers ───────────────────────────────────────────────────────────────

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
        assertEquals(HttpStatusCode.OK, response.status, "login $email failed: ${response.bodyAsText()}")
        return json.decodeFromString(response.bodyAsText())
    }

    private suspend fun ApplicationTestBuilder.createHouse(token: String): HouseResponse {
        val now = Clock.System.now()
        // adminUserId in body is intentionally omitted / set to 0 —
        // the backend must use the JWT user as admin, not the request field.
        val response = client.post("/houses") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""
                {
                  "title": "Test House",
                  "name": "Test House",
                  "adminUserId": 0,
                  "memberIds": [],
                  "description": "Integration test house",
                  "createTime": "$now",
                  "updateTime": "$now"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status, "createHouse failed: ${response.bodyAsText()}")
        return json.decodeFromString(response.bodyAsText())
    }

    private suspend fun ApplicationTestBuilder.addMember(token: String, houseId: Long, memberId: Long) {
        val response = client.put("/houses/$houseId/members/$memberId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status, "addMember failed: ${response.bodyAsText()}")
    }

    private suspend fun ApplicationTestBuilder.createTask(
        token: String,
        houseId: Long,
        ownerId: Long,
        assigneeIds: List<Long>,
        price: Double? = null,
        currencyCode: String? = null
    ): TaskResponse {
        val now = Clock.System.now()
        val priceField = if (price != null) ""","price":$price,"currencyCode":"$currencyCode"""" else ""
        val response = client.post("/houses/$houseId/tasks") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""
                {
                  "title": "Test Task",
                  "ownerId": $ownerId,
                  "description": "Test description",
                  "houseId": $houseId,
                  "accessLevel": "PUBLIC",
                  "assigneeIds": ${assigneeIds},
                  "dueTime": "$now"
                  $priceField
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, response.status, "createTask failed: ${response.bodyAsText()}")
        return json.decodeFromString(response.bodyAsText())
    }

    // ── Scenario 1: Create user → create house → create task ─────────────────

    @Test
    fun `scenario 1 - create user create house create task`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val auth = register("scenario1@test.com")
        val token = login("scenario1@test.com").accessToken
        val userId = auth.user.id

        val house = createHouse(token)
        assertTrue(house.id > 0, "House should be created with valid ID")

        val task = createTask(token, house.id, userId, listOf(userId))
        assertEquals("Test Task", task.title)
        assertEquals(house.id, task.houseId)
    }

    // ── Scenario 2: Create user → join house → create / assign task ───────────

    @Test
    fun `scenario 2 - create user join house create and assign task`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        // Admin creates house
        val adminAuth = register("admin-s2@test.com")
        val adminToken = login("admin-s2@test.com").accessToken
        val adminId = adminAuth.user.id
        val house = createHouse(adminToken)

        // Member registers and is added to house
        val memberAuth = register("member-s2@test.com")
        val memberId = memberAuth.user.id
        addMember(adminToken, house.id, memberId)

        // Admin creates task assigned to member
        val task = createTask(
            token = adminToken,
            houseId = house.id,
            ownerId = adminId,
            assigneeIds = listOf(memberId)
        )

        assertEquals("Test Task", task.title)
        assertTrue(task.assignees.any { it.userId == memberId }, "Member should be an assignee")
    }

    // ── Scenario 3: Create user → join house → settle cost / balance ──────────

    @Test
    fun `scenario 3 - create user join house settle cost and check balance`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        // Admin (payer) and member (executor)
        val adminAuth = register("admin-s3@test.com")
        val adminToken = login("admin-s3@test.com").accessToken
        val adminId = adminAuth.user.id
        val house = createHouse(adminToken)

        val memberAuth = register("member-s3@test.com")
        val memberId = memberAuth.user.id
        addMember(adminToken, house.id, memberId)

        // Admin creates task with price — admin paid 320 TWD
        val task = createTask(
            token = adminToken,
            houseId = house.id,
            ownerId = adminId,
            assigneeIds = listOf(memberId),
            price = 320.0,
            currencyCode = "TWD"
        )
        assertEquals(320.0, task.price)
        assertEquals("TWD", task.currencyCode)

        // Member pays back 100 TWD (partial settlement)
        val memberToken = login("member-s3@test.com").accessToken
        val settlementResponse = client.post("/houses/${house.id}/settlements") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $memberToken")
            setBody("""
                {
                  "payerId": $memberId,
                  "payeeId": $adminId,
                  "amount": 100.0,
                  "currencyCode": "TWD",
                  "note": "partial payment"
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, settlementResponse.status, "createSettlement failed: ${settlementResponse.bodyAsText()}")
        val settlement = json.decodeFromString<SettlementResponse>(settlementResponse.bodyAsText())
        assertEquals(100.0, settlement.amount)
        assertEquals("TWD", settlement.currencyCode)

        // Check balance — member still owes admin, displayed in USD
        val balanceResponse = client.get("/houses/${house.id}/settlements/balance?currency=USD") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }
        assertEquals(HttpStatusCode.OK, balanceResponse.status, "getBalance failed: ${balanceResponse.bodyAsText()}")

        val balanceJson = Json.parseToJsonElement(balanceResponse.bodyAsText()).jsonObject
        assertEquals("USD", balanceJson["displayCurrencyCode"]?.jsonPrimitive?.content)
        val balances = balanceJson["balances"]?.let {
            Json.decodeFromString<List<kotlinx.serialization.json.JsonElement>>(it.toString())
        }
        // After partial payment of 100 TWD, member still owes ~220 TWD ≈ 6.875 USD
        assertTrue(balances?.isNotEmpty() == true, "Should have outstanding balance")
    }
}
