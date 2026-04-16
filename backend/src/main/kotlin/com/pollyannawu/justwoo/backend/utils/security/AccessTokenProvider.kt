package com.pollyannawu.justwoo.backend.utils.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val algorithm: Algorithm,
)

interface AccessTokenProvider {
    fun createAccessToken(userId: Long): String
    fun verifyAccessToken(token: String): DecodedJWT?
}

class JwtAccessTokenProvider(private val config: JwtConfig) : AccessTokenProvider {
    private val verifier = JWT.require(config.algorithm)
        .withAudience(config.audience)
        .withIssuer(config.issuer)
        .build()

    override fun createAccessToken(userId: Long): String {
        println("Debug Algorithm Secret: ${config.algorithm}")
        return JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + TIMESTAMP_ONE_HOUR))
            .sign(config.algorithm)
    }

    override fun verifyAccessToken(token: String): DecodedJWT? {
        return try {
            println("Debug Algorithm Secret: ${config.algorithm}")
            verifier.verify(token)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object{
        private const val TIMESTAMP_ONE_HOUR = 3600000L
    }
}