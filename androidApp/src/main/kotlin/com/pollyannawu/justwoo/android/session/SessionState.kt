package com.pollyannawu.justwoo.android.session

import com.pollyannawu.justwoo.core.dto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Minimal in-memory session holder. Real token/user persistence lives in
 * [com.pollyannawu.justwoo.datasource.AuthDataSource] in the shared module;
 * this just tracks what the Android UI layer needs to route on.
 */
class SessionState {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _houseId = MutableStateFlow<Long?>(null)
    val houseId: StateFlow<Long?> = _houseId.asStateFlow()

    fun setUser(user: User?) { _user.value = user }
    fun setHouseId(id: Long?) { _houseId.value = id }
    fun clear() { _user.value = null; _houseId.value = null }
}
