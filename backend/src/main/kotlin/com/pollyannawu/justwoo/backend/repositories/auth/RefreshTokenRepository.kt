package com.pollyannawu.justwoo.backend.repositories.auth

import com.pollyannawu.justwoo.backend.schema.RefreshToken
import redis.clients.jedis.JedisPool
import kotlin.time.Duration

interface RefreshTokenRepository {
    suspend fun saveToken(userId: Long, deviceId: String, expireDuration: Duration): RefreshToken
    suspend fun findToken(token: String): RefreshToken?
    suspend fun deleteTokenByDevice(userId: String, deviceId: String)
    suspend fun deleteAllTokensByUser(userId: String)
}

class RedisRefreshTokenRepository(private val jedisPool: JedisPool) : RefreshTokenRepository {

    private fun getTokenKey(token: String) = "refresh_token:$token"
    private fun getUserTokensKey(userId: String) = "user_tokens:$userId"

    override suspend fun saveToken(
        userId: Long,
        deviceId: String,
        expireDuration: Duration
    ): RefreshToken {
        val token = createRefreshToken()
        val createAt = System.currentTimeMillis()
        val expireDurationInSeconds = expireDuration.inWholeSeconds
        val refreshToken =
            RefreshToken(token, userId, deviceId, expiresAt = createAt.plus(expireDurationInSeconds * 1000))

        jedisPool.resource.use { jedis ->
            val tokenKey = getTokenKey(token)
            val userKey = getUserTokensKey(userId.toString())

            // Save token details
            jedis.hset(tokenKey, refreshToken.toMap())
            jedis.expire(tokenKey, expireDurationInSeconds)

            // Track token for the user
            jedis.sadd(userKey, token)
            jedis.expire(userKey, expireDurationInSeconds)
        }
        return refreshToken
    }

    override suspend fun findToken(token: String): RefreshToken? {
        jedisPool.resource.use { jedis ->
            val data = jedis.hgetAll(getTokenKey(token))
            if (data.isEmpty()) return null
            return RefreshToken.fromMap(data)
        }
    }

    override suspend fun deleteTokenByDevice(userId: String, deviceId: String) {
        jedisPool.resource.use { jedis ->
            val userKey = getUserTokensKey(userId)
            val tokens = jedis.smembers(userKey)

            tokens.forEach { token ->
                val tokenKey = getTokenKey(token)
                val data = jedis.hgetAll(tokenKey)
                if (data["device"] == deviceId) {
                    jedis.del(tokenKey)
                    jedis.srem(userKey, token)
                }
            }
        }
    }

    override suspend fun deleteAllTokensByUser(userId: String) {
        jedisPool.resource.use { jedis ->
            val userKey = getUserTokensKey(userId)
            val tokens = jedis.smembers(userKey)

            tokens.forEach { token ->
                jedis.del(getTokenKey(token))
            }
            jedis.del(userKey)
        }
    }

    private fun createRefreshToken(): String {
        val randomBytes = ByteArray(RANDOM_BYTE_ARRAY_SIZE)
        java.security.SecureRandom().nextBytes(randomBytes)
        val newToken = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)

        return newToken
    }

    companion object {
        private const val RANDOM_BYTE_ARRAY_SIZE = 32
    }
}
