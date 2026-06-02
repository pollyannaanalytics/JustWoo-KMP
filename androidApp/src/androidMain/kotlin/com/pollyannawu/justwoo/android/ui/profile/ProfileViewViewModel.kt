package com.pollyannawu.justwoo.android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.domain.usecase.auth.ChangePasswordUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserEmailUseCase
import com.pollyannawu.justwoo.domain.usecase.profile.ObserveCurrentProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewViewModel(
    observeCurrentProfile: ObserveCurrentProfileUseCase,
    observeCurrentUserEmail: ObserveCurrentUserEmailUseCase,
    private val changePassword: ChangePasswordUseCase,
) : ViewModel() {

    val profile: StateFlow<Profile?> = observeCurrentProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val email: StateFlow<String?> = observeCurrentUserEmail()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    sealed interface PasswordDialogState {
        data object Hidden : PasswordDialogState
        data class Changing(
            val oldPassword: String = "",
            val newPassword: String = "",
            val confirmPassword: String = "",
            val isLoading: Boolean = false,
            val error: String? = null,
        ) : PasswordDialogState
        data object Success : PasswordDialogState
    }

    private val _passwordDialog = MutableStateFlow<PasswordDialogState>(PasswordDialogState.Hidden)
    val passwordDialog: StateFlow<PasswordDialogState> = _passwordDialog.asStateFlow()

    fun onChangePasswordClick() {
        _passwordDialog.value = PasswordDialogState.Changing()
    }

    fun onPasswordDialogDismiss() {
        _passwordDialog.value = PasswordDialogState.Hidden
    }

    fun onOldPasswordChange(v: String) = updateChanging { it.copy(oldPassword = v, error = null) }
    fun onNewPasswordChange(v: String) = updateChanging { it.copy(newPassword = v, error = null) }
    fun onConfirmPasswordChange(v: String) = updateChanging { it.copy(confirmPassword = v, error = null) }

    fun onSubmitPasswordChange() {
        val state = _passwordDialog.value as? PasswordDialogState.Changing ?: return
        if (state.newPassword != state.confirmPassword) {
            updateChanging { it.copy(error = "Passwords do not match.") }
            return
        }
        if (state.newPassword.length < 8) {
            updateChanging { it.copy(error = "Password must be at least 8 characters.") }
            return
        }
        updateChanging { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val success = changePassword(state.oldPassword, state.newPassword)
            if (success) {
                _passwordDialog.value = PasswordDialogState.Success
            } else {
                updateChanging { it.copy(isLoading = false, error = "Incorrect current password.") }
            }
        }
    }

    private fun updateChanging(transform: (PasswordDialogState.Changing) -> PasswordDialogState.Changing) {
        _passwordDialog.update { current ->
            if (current is PasswordDialogState.Changing) transform(current) else current
        }
    }
}
