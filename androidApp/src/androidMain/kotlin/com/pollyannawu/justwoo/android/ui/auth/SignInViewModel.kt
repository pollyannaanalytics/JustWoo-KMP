package com.pollyannawu.justwoo.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.data.AuthRepository
import com.pollyannawu.justwoo.model.AuthDataResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Collects email / password, delegates sign-in to [AuthRepository],
 * maps results to UI error states matching the Figma variants:
 *   - "e-mail not found"
 *   - "wrong password"
 */
class SignInViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    data class UiState(
        val email: String = "",
        val password: String = "",
        val showPassword: Boolean = false,
        val emailError: String? = null,
        val passwordError: String? = null,
        val loading: Boolean = false,
        val success: Boolean = false,
    ) {
        val canSubmit: Boolean
            get() = email.isNotBlank() && password.isNotBlank() && !loading
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }

    fun toggleShowPassword() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        _uiState.update { it.copy(loading = true, emailError = null, passwordError = null) }

        viewModelScope.launch {
            val result = authRepository.login(state.email.trim(), state.password)
            when (result) {
                is AuthDataResult.Success -> {
                    _uiState.update { it.copy(loading = false, success = true) }
                }
                // The shared layer currently only exposes NetworkFailure. Until it
                // differentiates error cases, we surface the generic "wrong password"
                // copy that the Figma variant shows — an obvious TODO for later.
                is AuthDataResult.Failure.NetworkFailure -> {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            passwordError = "Password incorrect.",
                        )
                    }
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(success = false) }
    }
}
