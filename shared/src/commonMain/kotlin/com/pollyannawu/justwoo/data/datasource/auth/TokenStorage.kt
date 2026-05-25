package com.pollyannawu.justwoo.data.datasource.auth

import com.pollyannawu.justwoo.data.network.data.AuthToken
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TokenStorage {
    /**
     * Reactive view of the persisted tokens. Emits null whenever tokens are
     * cleared (logout, refresh failure) so observers can react without
     * polling the underlying [Settings].
     */
    val tokensFlow: StateFlow<AuthToken?>
    fun getTokens(): AuthToken?
    fun saveTokens(tokens: AuthToken)
    fun clear()
}

class DefaultTokenStorage(
    private val settings: Settings,
) : TokenStorage {

    private val _tokensFlow = MutableStateFlow(readTokens())
    override val tokensFlow: StateFlow<AuthToken?> = _tokensFlow.asStateFlow()

    override fun getTokens(): AuthToken? = readTokens()

    override fun saveTokens(tokens: AuthToken) {
        settings.putString(KEY_ACCESS, tokens.accessToken)
        settings.putString(KEY_REFRESH, tokens.refreshToken)
        _tokensFlow.value = tokens
    }

    override fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
        _tokensFlow.value = null
    }

    private fun readTokens(): AuthToken? {
        val access = settings.getStringOrNull(KEY_ACCESS) ?: return null
        val refresh = settings.getStringOrNull(KEY_REFRESH) ?: return null
        return AuthToken(access, refresh)
    }

    private companion object {
        const val KEY_ACCESS = "auth.access_token"
        const val KEY_REFRESH = "auth.refresh_token"
    }
}
