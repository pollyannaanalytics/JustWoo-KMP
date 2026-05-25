package com.pollyannawu.justwoo.data.datasource.auth

import com.pollyannawu.justwoo.core.dto.User
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getLongOrNullFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

interface UserStorage {
    val userFlow: Flow<User?>
    fun getUser(): User?
    fun saveUser(user: User)

    val houseIdFlow: Flow<Long?>
    fun getHouseId(): Long?
    fun saveHouseId(houseId: Long)

    /**
     * Sticky "user has had at least one successful sign-in / sign-up on
     * this install" flag. Survives [clear] so logging out doesn't put a
     * returning user back into the onboarding flow.
     */
    fun hasOnboarded(): Boolean
    fun markOnboarded()

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

    override val houseIdFlow: Flow<Long?> = settings.getLongOrNullFlow(KEY_HOUSE_ID)

    override fun getHouseId(): Long? = settings.getLongOrNull(KEY_HOUSE_ID)

    override fun saveHouseId(houseId: Long) {
        settings.putLong(KEY_HOUSE_ID, houseId)
    }

    override fun hasOnboarded(): Boolean = settings.getBoolean(KEY_HAS_ONBOARDED, false)

    override fun markOnboarded() {
        settings.putBoolean(KEY_HAS_ONBOARDED, true)
    }

    override fun clear() {
        settings.remove(KEY_USER)
        settings.remove(KEY_HOUSE_ID)
        // Intentionally NOT removing KEY_HAS_ONBOARDED — a returning user
        // who logs out should still land on Sign in, not the Register
        // onboarding screen.
    }

    private companion object {
        const val KEY_USER = "auth.user"
        const val KEY_HOUSE_ID = "auth.houseId"
        const val KEY_HAS_ONBOARDED = "auth.has_onboarded"
    }
}
