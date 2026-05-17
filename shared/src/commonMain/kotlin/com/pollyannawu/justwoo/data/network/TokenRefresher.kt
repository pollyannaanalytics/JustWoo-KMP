package com.pollyannawu.justwoo.data.network

import com.pollyannawu.justwoo.core.dto.RefreshRequest
import com.pollyannawu.justwoo.core.dto.RefreshResponse
import com.pollyannawu.justwoo.data.datasource.auth.DeviceIdProvider
import com.pollyannawu.justwoo.data.network.data.AuthToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CancellationException

interface TokenRefresher {
    suspend fun refresh(refreshToken: String): AuthToken?
}

class DefaultTokenRefresher(
    private val client: HttpClient,
    private val deviceIdProvider: DeviceIdProvider,
) : TokenRefresher {

    override suspend fun refresh(refreshToken: String): AuthToken? =
        try {
            val response: RefreshResponse = client.post("/auth/refresh") {
                setBody(RefreshRequest(deviceId = deviceIdProvider.get(), token = refreshToken))
            }.body()
            AuthToken(
                accessToken = response.accessToken,
                refreshToken = response.token.refreshToken,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
}
