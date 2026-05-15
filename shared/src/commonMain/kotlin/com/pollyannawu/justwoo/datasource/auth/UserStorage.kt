package com.pollyannawu.justwoo.datasource.auth

import com.pollyannawu.justwoo.core.dto.User
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

interface UserStorage {
    val userFlow: Flow<User?>
    fun getUser(): User?
    fun saveUser(user: User)
    fun clear()
}

@OptIn(ExperimentalSettingsApi::class)
class DefaultUserStorage(
    private val settings: ObservableSettings,
    private val json: Json,
) : UserStorage {

    override val userFlow: Flow<User?> = settings
        .getStringOrNullFlow(KEY_USER)
        .map { raw -> raw?.let { json.decodeFromString<User>(it) } }

    override fun getUser(): User? =
        settings.getStringOrNull(KEY_USER)?.let { json.decodeFromString<User>(it) }

    override fun saveUser(user: User) {
        settings.putString(KEY_USER, json.encodeToString(User.serializer(), user))
    }

    override fun clear() {
        settings.remove(KEY_USER)
    }

    private companion object {
        const val KEY_USER = "auth.user"
    }
}
