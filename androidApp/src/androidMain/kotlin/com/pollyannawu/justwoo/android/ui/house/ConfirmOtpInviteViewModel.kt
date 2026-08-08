package com.pollyannawu.justwoo.android.ui.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ConfirmOtpInviteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfirmOtpInviteViewModel(
    private val confirmOtpInviteUseCase: ConfirmOtpInviteUseCase,
    private val getCurrentHouseIdUseCase: GetCurrentHouseIdUseCase,
) : ViewModel() {

    sealed interface ConfirmOtpInviteUiState {
        data object Idle : ConfirmOtpInviteUiState
        data object Loading : ConfirmOtpInviteUiState
        data object Success : ConfirmOtpInviteUiState
        data class Error(val message: String) : ConfirmOtpInviteUiState
    }

    private val _uiState = MutableStateFlow<ConfirmOtpInviteUiState>(ConfirmOtpInviteUiState.Idle)
    val uiState: StateFlow<ConfirmOtpInviteUiState> = _uiState.asStateFlow()

    fun confirm(code: String, displayNumber: Int) {
        val houseId = getCurrentHouseIdUseCase()
        if (houseId == null) {
            _uiState.value = ConfirmOtpInviteUiState.Error("No active house. Please join or create a house first.")
            return
        }
        _uiState.value = ConfirmOtpInviteUiState.Loading
        viewModelScope.launch {
            try {
                confirmOtpInviteUseCase(code, displayNumber, houseId)
                _uiState.value = ConfirmOtpInviteUiState.Success
            } catch (e: Exception) {
                _uiState.value = ConfirmOtpInviteUiState.Error(
                    e.message ?: "Invalid or expired code."
                )
            }
        }
    }
}
