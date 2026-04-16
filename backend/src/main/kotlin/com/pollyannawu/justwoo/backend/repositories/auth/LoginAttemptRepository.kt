package com.pollyannawu.justwoo.backend.repositories.auth

import com.pollyannawu.justwoo.backend.schema.LoginAttemptStatus
import redis.clients.jedis.JedisPool
import kotlinx.datetime.Clock
import kotlin.time.Duration



interface LoginAttemptRepository {
    suspend fun getLoginAttempt(email: String, expireTime: Duration): LoginAttemptStatus?
    suspend fun recordLoginAttempt(email: String, expireTime: Duration)
    suspend fun clearLoginAttempt(email: String)
}

internal class RedisLoginAttemptRepository (private val jedisPool: JedisPool) : LoginAttemptRepository {

    private fun getKey(email: String) = "login_attempt:$email"


    override suspend fun getLoginAttempt(email: String, expireTime: Duration): LoginAttemptStatus? {
        jedisPool.resource.use { jedis ->
            val key = getKey(email)
            val data = jedis.hgetAll(key)
            if (data.isEmpty()) return null

            val status = LoginAttemptStatus.fromMap(data) ?: return null

            val now = Clock.System.now()
            if (now - status.lastAttemptTime > expireTime) {
                jedis.del(key)
                return null
            }
            return status
        }
    }


    override suspend fun recordLoginAttempt(email: String, expireTime: Duration) {
        jedisPool.resource.use { jedis ->
            val key = getKey(email)
            val currentStatus = getLoginAttempt(email, expireTime)
            val now = Clock.System.now()

            val newStatus = if (currentStatus == null) {
                LoginAttemptStatus(1, now)
            } else {
                LoginAttemptStatus(currentStatus.count + 1, now)
            }

            jedis.hset(key, newStatus.toMap())
            jedis.expire(key, expireTime.inWholeSeconds)

        }
    }

    override suspend fun clearLoginAttempt(email: String) {
        jedisPool.resource.use { jedis ->
            jedis.del(getKey(email))
        }
    }
}