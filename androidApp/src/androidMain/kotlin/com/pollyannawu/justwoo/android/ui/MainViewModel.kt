package com.pollyannawu.justwoo.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.HasActiveSessionUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveIsAuthenticatedUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ObserveHouseMembersUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ResolveCurrentHouseUseCase
import com.pollyannawu.justwoo.ui.nav.auth.AuthStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    observeCurrentUserId: ObserveCurrentUserIdUseCase,
    observeCurrentHouseId: ObserveCurrentHouseIdUseCase,
    observeIsAuthenticated: ObserveIsAuthenticatedUseCase,
    private val hasActiveSession: HasActiveSessionUseCase,
    private val getCurrentHouseId: GetCurrentHouseIdUseCase,
    private val resolveCurrentHouse: ResolveCurrentHouseUseCase,
    observeHouseMembers: ObserveHouseMembersUseCase,
) : ViewModel() {

    val currentUserId: StateFlow<Long?> = observeCurrentUserId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val currentHouseId: StateFlow<Long?> = observeCurrentHouseId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val userRole: StateFlow<MemberRole?> = combine(
        observeCurrentUserId(),
        observeCurrentHouseId(),
    ) { userId, houseId -> Pair(userId, houseId) }
        .flatMapLatest { (userId, houseId) ->
            if (userId == null || houseId == null) flowOf(null)
            else observeHouseMembers(houseId).map { members ->
                members.firstOrNull { it.userId == userId }?.role
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val isAdmin: StateFlow<Boolean> = userRole
        .map { it == MemberRole.ADMIN }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

    val isAuthenticated: StateFlow<Boolean> = observeIsAuthenticated()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = hasActiveSession(),
        )

    // True while we're querying the server to find out if the user has a house.
    // Starts as true if the user is authenticated but has no local houseId, so the
    // loading screen is shown immediately without a flash of HouseOnboarding.
    private val _isCheckingHouse = MutableStateFlow(
        hasActiveSession() && getCurrentHouseId() == null
    )

    // Prevents re-running the check if isAuthenticated emits multiple true values.
    private var houseCheckDone = false

    val navCommand: StateFlow<NavCommand> = combine(
        observeIsAuthenticated(),
        observeCurrentHouseId(),
        _isCheckingHouse,
    ) { isAuth, houseId, checking ->
        when {
            !isAuth -> NavCommand.ToAuth
            checking -> NavCommand.Loading
            houseId == null -> NavCommand.ToHouseOnboarding
            else -> NavCommand.ToHome
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = when {
            !hasActiveSession() -> NavCommand.ToAuth
            getCurrentHouseId() != null -> NavCommand.ToHome
            else -> NavCommand.Loading
        },
    )

    init {
        viewModelScope.launch {
            observeIsAuthenticated().collect { isAuth ->
                if (isAuth && getCurrentHouseId() == null && !houseCheckDone) {
                    houseCheckDone = true
                    _isCheckingHouse.value = true
                    try { resolveCurrentHouse() } catch (_: Exception) {}
                    _isCheckingHouse.value = false
                }
                if (!isAuth) {
                    houseCheckDone = false
                    _isCheckingHouse.value = false
                }
            }
        }
    }

    sealed interface NavCommand {
        data object ToAuth : NavCommand
        data object Loading : NavCommand
        data object ToHouseOnboarding : NavCommand
        data object ToHome : NavCommand
    }

    fun resolveAuthStart(): AuthStart = AuthStart.SignIn
}
