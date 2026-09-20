package com.pollyannawu.justwoo.android.ui.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.domain.usecase.house.CreateEmailInvitationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InviteByEmailViewModel(
    private val createEmailInvitationUseCase: CreateEmailInvitationUseCase,
) : ViewModel() {

    sealed interface UiState {
        data class EnteringEmail(
            val email: String = "",
            val loading: Boolean = false,
            val error: String? = null,
        ) : UiState

        data class CodeGenerated(
            val code: String,
            val houseName: String,
        ) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.EnteringEmail())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        val current = _uiState.value as? UiState.EnteringEmail ?: return
        _uiState.update { current.copy(email = value, error = null) }
    }

    fun sendInvite(houseId: Long) {
        val current = _uiState.value as? UiState.EnteringEmail ?: return
        val email = current.email.trim()

        if (!isValidEmail(email)) {
            _uiState.update { current.copy(error = "Please enter a valid email address") }
            return
        }

        _uiState.update { current.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = createEmailInvitationUseCase(houseId, email)
                _uiState.update { UiState.CodeGenerated(code = response.code, houseName = response.houseName) }
            } catch (e: Exception) {
                _uiState.update { current.copy(loading = false, error = "Failed to create invitation. Please try again.") }
            }
        }
    }

    fun reset() {
        _uiState.update { UiState.EnteringEmail() }
    }

    private fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(email)
}
