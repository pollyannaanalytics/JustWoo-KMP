package com.pollyannawu.justwoo.backend.database

import io.ktor.server.config.ApplicationConfig
import redis.clients.jedis.JedisPool

object RedisFactory {
    private const val REDIS_HOST_PATH = "redis.host"
    private const val REDIS_PORT_PATH = "redis.port"

    private lateinit var jedisPool: JedisPool

    fun init(config: ApplicationConfig) {
        val host = config.propertyOrNull(REDIS_HOST_PATH)?.getString() ?: "localhost"
        val port = config.propertyOrNull(REDIS_PORT_PATH)?.getString()?.toIntOrNull() ?: 6379
        jedisPool = JedisPool(host, port)
    }

    fun getPool(): JedisPool = jedisPool
}