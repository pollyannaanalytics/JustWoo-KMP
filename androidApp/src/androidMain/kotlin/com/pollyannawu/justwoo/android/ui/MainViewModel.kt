package com.pollyannawu.justwoo.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.domain.usecase.auth.HasActiveSessionUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.HasOnboardedUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveIsAuthenticatedUseCase
import com.pollyannawu.justwoo.ui.nav.auth.AuthStart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
) : ViewModel() {

    val currentUserId: StateFlow<Long?> = observeCurrentUserId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val currentHouseId: StateFlow<Long?> = observeCurrentHouseId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    /**
     * Reactive session predicate. The initial value is a synchronous read
     * of prefs so first composition can pick the right Decompose config
     * without flashing the wrong screen.
     */
    val isAuthenticated: StateFlow<Boolean> = observeIsAuthenticated()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = hasActiveSession(),
        )

    /**
     * Fresh installs land on Register (onboarding); a user who has ever
     * signed in/up on this device sees Sign in by default. Re-evaluated
     * each time the auth sub-stack is constructed.
     */
    fun resolveAuthStart(): AuthStart =
        if (hasOnboarded()) AuthStart.SignIn else AuthStart.Register
}
