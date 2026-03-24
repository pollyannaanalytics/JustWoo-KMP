package com.pollyannawu.justwoo.backend.utils.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.ApplicationConfig
import java.util.Date

interface SecurityService {
   fun createToken(userId: String): String
   fun verifyToken(token: String): Map<String, String>?
}

class JwtService(private val config: ApplicationConfig): SecurityService {
    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()

    override fun createToken(userId: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(Algorithm.HMAC256(secret))
    }

    override fun verifyToken(token: String): Map<String, String>? {
            return try {
                val verifier = JWT.require(Algorithm.HMAC256(secret)).build()
                val decoded = verifier.verify(token)
                decoded.claims.mapValues { it.value.asString() }
            } catch (e: Exception) {
                null
            }
    }
}