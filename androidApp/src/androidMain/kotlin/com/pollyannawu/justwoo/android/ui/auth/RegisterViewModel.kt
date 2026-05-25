package com.pollyannawu.justwoo.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.domain.usecase.auth.RegisterOutcome
import com.pollyannawu.justwoo.domain.usecase.auth.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Collects email / password / confirm and hands off to [RegisterUseCase].
 * All validation + AuthDataResult mapping lives in the use case — this
 * ViewModel only translates a [RegisterOutcome] into UI state.
 */
class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
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
    fun onPasswordChange(v: String) =
        _uiState.update { it.copy(password = v, passwordError = null, confirmError = null) }
    fun onConfirmChange(v: String) =
        _uiState.update { it.copy(confirmPassword = v, confirmError = null) }
    fun toggleShowPassword() = _uiState.update { it.copy(showPassword = !it.showPassword) }

    fun submit() {
        val s = _uiState.value
        if (!s.canSubmit) return

        _uiState.update {
            it.copy(loading = true, emailError = null, passwordError = null, confirmError = null)
        }

        viewModelScope.launch {
            when (registerUseCase(s.email, s.password, s.confirmPassword)) {
                is RegisterOutcome.Success ->
                    _uiState.update { it.copy(loading = false, success = true) }

                RegisterOutcome.Failure.InvalidEmail ->
                    _uiState.update {
                        it.copy(loading = false, emailError = "Please enter a valid e-mail.")
                    }

                RegisterOutcome.Failure.WeakPassword ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            passwordError = "Required at least $MIN_PASSWORD_LENGTH characters",
                        )
                    }

                RegisterOutcome.Failure.PasswordMismatch ->
                    _uiState.update {
                        it.copy(loading = false, confirmError = "Passwords do not match.")
                    }

                RegisterOutcome.Failure.EmailAlreadyRegistered ->
                    _uiState.update {
                        it.copy(loading = false, emailError = "This e-mail is already registered.")
                    }

                RegisterOutcome.Failure.InvalidRequest ->
                    _uiState.update {
                        it.copy(loading = false, emailError = "Invalid sign-up details.")
                    }

                RegisterOutcome.Failure.Network ->
                    _uiState.update {
                        it.copy(loading = false, emailError = "Couldn't create account — please try again.")
                    }
            }
        }
    }

    fun consumeSuccess() = _uiState.update { it.copy(success = false) }

    companion object {
        /** Re-exposed for the screen's helper-text label. */
        const val MIN_PASSWORD_LENGTH = RegisterUseCase.MIN_PASSWORD_LENGTH
    }
}
