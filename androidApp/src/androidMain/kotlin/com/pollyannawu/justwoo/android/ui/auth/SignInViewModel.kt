package com.pollyannawu.justwoo.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.domain.usecase.auth.LoginOutcome
import com.pollyannawu.justwoo.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SignInViewModel(
    private val loginUseCase: LoginUseCase,
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
            when (val outcome = loginUseCase(state.email, state.password)) {
                is LoginOutcome.Success ->
                    _uiState.update { it.copy(loading = false, success = true) }

                LoginOutcome.Failure.InvalidEmail ->
                    _uiState.update {
                        it.copy(loading = false, emailError = "Please enter a valid e-mail.")
                    }

                LoginOutcome.Failure.InvalidCredentials ->
                    _uiState.update {
                        it.copy(loading = false, passwordError = "Password incorrect.")
                    }

                LoginOutcome.Failure.TooManyAttempts ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            passwordError = "Too many attempts — try again later.",
                        )
                    }

                LoginOutcome.Failure.Network ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            passwordError = "Couldn't sign in — please try again.",
                        )
                    }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(success = false) }
    }
}
