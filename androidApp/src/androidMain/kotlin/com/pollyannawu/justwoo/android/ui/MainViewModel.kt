package com.pollyannawu.justwoo.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.data.AuthRepository
import com.pollyannawu.justwoo.ui.nav.auth.AuthStart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Activity-scoped session/state holder. Acts as the boundary between the
 * data layer and the Decompose nav graph: nothing under `ui/nav/` should
 * import [AuthRepository] — they observe state through this VM instead.
 */
class MainViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val currentUserId: StateFlow<Long?> = authRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    val currentHouseId: StateFlow<Long?> = authRepository.currentHouseId
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    /**
     * Reactive session predicate. The initial value is a synchronous read
     * of prefs so first composition can pick the right Decompose config
     * without flashing the wrong screen.
     */
    val isAuthenticated: StateFlow<Boolean> = authRepository.isAuthenticated
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = authRepository.hasActiveSession(),
        )

    /**
     * Fresh installs land on Register (onboarding); a user who has ever
     * signed in/up on this device sees Sign in by default. Re-evaluated
     * each time the auth sub-stack is constructed.
     */
    fun resolveAuthStart(): AuthStart =
        if (authRepository.hasOnboarded()) AuthStart.SignIn else AuthStart.Register
}
