package com.pollyannawu.justwoo.datasource

import com.pollyannawu.justwoo.core.dto.User
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    fun getToken(): String?
    fun getUser(): Flow<User?>
    fun getDeviceId(): String

    suspend fun saveAccessToken(token: String)
    suspend fun saveRefreshToken(token: String)
    suspend fun saveUser(user: User)
    suspend fun removeToken()
    suspend fun clearAll()
}