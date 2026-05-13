package com.pollyannawu.justwoo.datasource

import com.pollyannawu.justwoo.network.data.AuthToken
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface TokenStorage {
    val tokens: Flow<AuthToken?>
    suspend fun getTokens(): AuthToken?
    suspend fun saveTokens(tokens: AuthToken)
    suspend fun clear()
}

@OptIn(ExperimentalSettingsApi::class)
class DefaultTokenStorage(
    private val settings: FlowSettings,
) : TokenStorage {

    override val tokens: Flow<AuthToken?> =
        settings.getStringOrNullFlow(KEY_ACCESS)
            .combine(settings.getStringOrNullFlow(KEY_REFRESH)) { access, refresh ->
                if (access != null && refresh != null) AuthToken(access, refresh) else null
            }

    override suspend fun getTokens(): AuthToken? {
        val access = settings.getStringOrNull(KEY_ACCESS) ?: return null
        val refresh = settings.getStringOrNull(KEY_REFRESH) ?: return null
        return AuthToken(access, refresh)
    }

    override suspend fun saveTokens(tokens: AuthToken) {
        settings.putString(KEY_REFRESH, tokens.refreshToken)
        settings.putString(KEY_ACCESS, tokens.accessToken)
    }

    override suspend fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
    }

    private companion object {
        const val KEY_ACCESS = "auth.access_token"
        const val KEY_REFRESH = "auth.refresh_token"
    }
}