package com.pollyannawu.justwoo.session

import com.pollyannawu.justwoo.core.dto.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory session holder used by the shared navigation root. The persistent
 * counterparts (TokenStorage / UserStorage) live alongside the data layer — this
 * is just the slice the navigation graph routes on.
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
