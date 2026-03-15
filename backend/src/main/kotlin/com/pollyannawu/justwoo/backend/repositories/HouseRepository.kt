package com.pollyannawu.justwoo.backend.repositories

interface HouseRepository {
    suspend fun isMember(userId: Long, houseId: Long): Boolean
}