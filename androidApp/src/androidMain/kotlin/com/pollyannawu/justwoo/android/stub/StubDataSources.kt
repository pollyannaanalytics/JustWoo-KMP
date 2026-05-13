package com.pollyannawu.justwoo.android.stub

import com.pollyannawu.justwoo.core.dto.User
import com.pollyannawu.justwoo.datasource.AuthDataSource

class StubAuthDataSource(
    private val fixedDeviceId: String = "android-stub-device",
) : AuthDataSource {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var user: User? = null

    override fun getToken(): String? = accessToken
    override fun getUser(): User? = user
    override fun getDeviceId(): String = fixedDeviceId

    override suspend fun saveAccessToken(token: String) { accessToken = token }
    override suspend fun saveRefreshToken(token: String) { refreshToken = token }
    override suspend fun saveUser(user: User) { this.user = user }
    override suspend fun removeToken() { accessToken = null; refreshToken = null }
    override suspend fun clearAll() { accessToken = null; refreshToken = null; user = null }
}
