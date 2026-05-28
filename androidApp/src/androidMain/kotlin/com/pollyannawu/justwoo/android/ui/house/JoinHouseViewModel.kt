package com.pollyannawu.justwoo.android.ui.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.JoinRequestStatus
import com.pollyannawu.justwoo.domain.usecase.house.GetJoinRequestStatusUseCase
import com.pollyannawu.justwoo.domain.usecase.house.SubmitJoinRequestUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinHouseViewModel(
    private val submitJoinRequestUseCase: SubmitJoinRequestUseCase,
    private val getJoinRequestStatusUseCase: GetJoinRequestStatusUseCase,
) : ViewModel() {

    sealed interface JoinUiState {
        data class EnteringCode(
            val code: String = "",
            val loading: Boolean = false,
            val error: String? = null,
        ) : JoinUiState

        data object WaitingApproval : JoinUiState
        data object Approved : JoinUiState
        data class Rejected(val message: String) : JoinUiState
    }

    private val _uiState = MutableStateFlow<JoinUiState>(JoinUiState.EnteringCode())
    val uiState: StateFlow<JoinUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    fun onCodeChange(value: String) {
        val current = _uiState.value
        if (current is JoinUiState.EnteringCode) {
            _uiState.update { JoinUiState.EnteringCode(code = value, error = null) }
        }
    }

    fun submit() {
        val current = _uiState.value as? JoinUiState.EnteringCode ?: return
        val code = current.code.trim()
        if (code.isBlank()) return

        _uiState.update { JoinUiState.EnteringCode(code = code, loading = true) }
        viewModelScope.launch {
            try {
                submitJoinRequestUseCase(code)
                _uiState.update { JoinUiState.WaitingApproval }
                startPolling()
            } catch (e: Exception) {
                _uiState.update {
                    JoinUiState.EnteringCode(
                        code = "",
                        error = "Code is invalid or expired. Ask your admin to generate a new one.",
                    )
                }
            }
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(5_000)
                try {
                    val response = getJoinRequestStatusUseCase()
                    when (response?.status) {
                        JoinRequestStatus.APPROVED -> {
                            _uiState.update { JoinUiState.Approved }
                            return@launch
                        }
                        JoinRequestStatus.REJECTED -> {
                            _uiState.update {
                                JoinUiState.Rejected("Your request was rejected by the admin.")
                            }
                            return@launch
                        }
                        else -> Unit
                    }
                } catch (_: Exception) {
                    // transient network error — keep polling
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
