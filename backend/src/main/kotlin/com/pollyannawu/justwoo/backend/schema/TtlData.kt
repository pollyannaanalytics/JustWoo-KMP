package com.pollyannawu.justwoo.backend.schema

import kotlinx.datetime.Instant


data class LoginAttemptStatus (
    val count: Int,
    val lastAttemptTime: Instant
) {

    fun toMap(): Map<String, String> = mapOf(
        "count" to count.toString(),
        "lastAttemptTime" to lastAttemptTime.toString()
    )

    companion object {
        fun fromMap(map: Map<String, String>): LoginAttemptStatus? {
            val count = map["count"]?.toIntOrNull() ?: return null
            val time = map["lastAttemptTime"]?.let { kotlinx.datetime.Instant.parse(it) } ?: return null
            return LoginAttemptStatus(count, time)
        }
    }
}

data class RefreshToken(
    val token: String,
    val userId: Long,
    val device: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = 0L
) {
    fun toMap(): Map<String, String> = mapOf(
        "userId" to userId.toString(),
        "token" to token,
        "device" to device,
        "createdAt" to createdAt.toString(),
        "createdAt" to createdAt.toString()
    )

    companion object {
        fun fromMap(map: Map<String, String>): RefreshToken? {
            val token = map["token"] ?: return null
            val userId = map["userId"]?.toLongOrNull() ?: return null
            val device = map["device"] ?: return null
            val createdAt = map["createdAt"]?.toLongOrNull() ?: return null
            val expiresAt = map["expiresAt"]?.toLongOrNull() ?: return null
            return RefreshToken(token, userId, device, createdAt, expiresAt)
        }
    }
}