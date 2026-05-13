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

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    data class UiState(
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val showPassword: Boolean = false,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmError: String? = null,
        val loading: Boolean = false,
        val success: Boolean = false,
    ) {
        val canSubmit: Boolean
            get() = email.isNotBlank() &&
                    password.isNotBlank() &&
                    confirmPassword.isNotBlank() &&
                    !loading
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, passwordError = null, confirmError = null) }
    fun onConfirmChange(v: String) = _uiState.update { it.copy(confirmPassword = v, confirmError = null) }
    fun toggleShowPassword() = _uiState.update { it.copy(showPassword = !it.showPassword) }

    fun submit() {
        val s = _uiState.value
        if (!s.canSubmit) return
        if (!s.email.contains("@")) {
            _uiState.update { it.copy(emailError = "Please enter a valid e-mail.") }
            return
        }
        if (s.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters.") }
            return
        }
        if (s.password != s.confirmPassword) {
            _uiState.update { it.copy(confirmError = "Passwords do not match.") }
            return
        }

        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            when (authRepository.register(s.email.trim(), s.password)) {
                is AuthDataResult.Success ->
                    _uiState.update { it.copy(loading = false, success = true) }
                is AuthDataResult.Failure.NetworkFailure ->
                    _uiState.update {
                        it.copy(loading = false, emailError = "Couldn't create account — please try again.")
                    }
            }
        }
    }

    fun consumeSuccess() = _uiState.update { it.copy(success = false) }
}
