package com.pollyannawu.justwoo.data.datasource.auth

import com.pollyannawu.justwoo.data.network.data.AuthToken
import com.russhwolf.settings.Settings

interface TokenStorage {
    fun getTokens(): AuthToken?
    fun saveTokens(tokens: AuthToken)
    fun clear()
}

class DefaultTokenStorage(
    private val settings: Settings,
) : TokenStorage {

    override fun getTokens(): AuthToken? {
        val access = settings.getStringOrNull(KEY_ACCESS) ?: return null
        val refresh = settings.getStringOrNull(KEY_REFRESH) ?: return null
        return AuthToken(access, refresh)
    }

    override fun saveTokens(tokens: AuthToken) {
        settings.putString(KEY_ACCESS, tokens.accessToken)
        settings.putString(KEY_REFRESH, tokens.refreshToken)
    }

    override fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
    }

    private companion object {
        const val KEY_ACCESS = "auth.access_token"
        const val KEY_REFRESH = "auth.refresh_token"
    }
}