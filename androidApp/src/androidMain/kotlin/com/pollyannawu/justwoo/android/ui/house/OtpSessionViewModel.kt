package com.pollyannawu.justwoo.android.ui.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.dto.OtpInviteSession
import com.pollyannawu.justwoo.domain.usecase.house.GenerateOtpSessionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtpSessionViewModel(
    private val generateOtpSessionUseCase: GenerateOtpSessionUseCase,
) : ViewModel() {

    sealed interface OtpSessionUiState {
        data object Loading : OtpSessionUiState
        data class Success(
            val session: OtpInviteSession,
            val remainingSeconds: Int,
            val canRefresh: Boolean = false,
        ) : OtpSessionUiState
        data class Error(val message: String?) : OtpSessionUiState
    }

    private val _uiState = MutableStateFlow<OtpSessionUiState>(OtpSessionUiState.Loading)
    val uiState: StateFlow<OtpSessionUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        loadSession()
    }

    /** Only callable when the countdown has expired (canRefresh == true). */
    fun refresh() {
        val current = _uiState.value
        if (current is OtpSessionUiState.Success && !current.canRefresh) return
        if (current is OtpSessionUiState.Loading) return
        countdownJob?.cancel()
        loadSession()
    }

    private fun loadSession() {
        _uiState.value = OtpSessionUiState.Loading
        viewModelScope.launch {
            try {
                val session = generateOtpSessionUseCase()
                _uiState.value = OtpSessionUiState.Success(session, session.expiresInSeconds)
                startCountdown(session)
            } catch (e: Exception) {
                _uiState.value = OtpSessionUiState.Error(e.message)
            }
        }
    }

    private fun startCountdown(session: OtpInviteSession) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = session.expiresInSeconds
            while (remaining > 0) {
                delay(1_000L)
                remaining--
                val current = _uiState.value
                if (current is OtpSessionUiState.Success) {
                    _uiState.value = current.copy(
                        remainingSeconds = remaining,
                        canRefresh = remaining == 0,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
