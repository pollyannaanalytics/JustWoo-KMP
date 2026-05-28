package com.pollyannawu.justwoo.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.domain.usecase.auth.HasActiveSessionUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.HasOnboardedUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveIsAuthenticatedUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ObserveHouseMembersUseCase
import com.pollyannawu.justwoo.ui.nav.auth.AuthStart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Activity-scoped session/state holder. Acts as the boundary between the
 * data layer and the Decompose nav graph: nothing under `ui/nav/` should
 * see the repository — they observe state through this VM (via use cases).
 */
class MainViewModel(
    observeCurrentUserId: ObserveCurrentUserIdUseCase,
    observeCurrentHouseId: ObserveCurrentHouseIdUseCase,
    observeIsAuthenticated: ObserveIsAuthenticatedUseCase,
    private val hasActiveSession: HasActiveSessionUseCase,
    private val hasOnboarded: HasOnboardedUseCase,
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

    /**
     * Reactive session predicate. Kept for the synchronous initial-config
     * read in MainActivity (`isAuthenticated.value`) — not observed reactively.
     */
    val isAuthenticated: StateFlow<Boolean> = observeIsAuthenticated()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = hasActiveSession(),
        )

    /**
     * Single nav decision derived from auth + house state.
     * MainActivity observes this and calls the matching RootComponent hook.
     */
    val navCommand: StateFlow<NavCommand> = combine(
        observeIsAuthenticated(),
        observeCurrentHouseId(),
    ) { isAuth, houseId ->
        when {
            !isAuth -> NavCommand.ToAuth
            houseId == null -> NavCommand.ToHouseOnboarding
            else -> NavCommand.ToHome
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        initialValue = if (hasActiveSession()) NavCommand.ToHouseOnboarding else NavCommand.ToAuth,
    )

    sealed interface NavCommand {
        data object ToAuth : NavCommand
        data object ToHouseOnboarding : NavCommand
        data object ToHome : NavCommand
    }

    /**
     * Fresh installs land on Register (onboarding); a user who has ever
     * signed in/up on this device sees Sign in by default. Re-evaluated
     * each time the auth sub-stack is constructed.
     */
    fun resolveAuthStart(): AuthStart =
        if (hasOnboarded()) AuthStart.SignIn else AuthStart.Register
}
