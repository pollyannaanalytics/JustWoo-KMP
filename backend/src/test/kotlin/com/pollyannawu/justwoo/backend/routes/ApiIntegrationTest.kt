package com.pollyannawu.justwoo.backend.routes

import com.pollyannawu.justwoo.backend.diModule
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiIntegrationTest {

    private fun testConfig() = MapApplicationConfig(
        "database.host" to "mem",           // triggers H2 in-memory mode in DatabaseFactory
        "database.port" to "5432",
        "database.dbName" to "justwoo",
        "database.user" to "",
        "database.password" to "",
        "jwt.secret" to "test-secret-that-is-long-enough-32ch",
        "jwt.issuer" to "https://justwoo.test",
        "jwt.audience" to "justwoo-test",
        "jwt.realm" to "test",
        "redis.host" to "localhost",
        "redis.port" to "6379"
    )

    @Test
    fun `GET hello returns 200`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val response = client.get("/hello")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().isNotBlank())
    }

    @Test
    fun `GET swagger returns 200`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val response = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST register with invalid body returns 400`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val response = client.post("/auth/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("{}")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST login with invalid body returns 400`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val response = client.post("/auth/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("{}")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET protected endpoint without token returns 401`() = testApplication {
        environment { config = testConfig() }
        application { diModule() }

        val response = client.get("/houses")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
