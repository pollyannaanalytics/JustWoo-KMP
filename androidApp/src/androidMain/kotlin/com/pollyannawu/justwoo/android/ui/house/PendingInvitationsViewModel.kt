package com.pollyannawu.justwoo.android.ui.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.dto.EmailInvitationResponse
import com.pollyannawu.justwoo.domain.usecase.house.GetMyEmailInvitationsUseCase
import com.pollyannawu.justwoo.domain.usecase.house.JoinViaEmailInviteUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ResolveCurrentHouseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PendingInvitationsViewModel(
    private val getMyEmailInvitationsUseCase: GetMyEmailInvitationsUseCase,
    private val joinViaEmailInviteUseCase: JoinViaEmailInviteUseCase,
    private val resolveCurrentHouseUseCase: ResolveCurrentHouseUseCase,
) : ViewModel() {

    sealed interface ListUiState {
        data object Loading : ListUiState
        data class Invitations(val invitations: List<EmailInvitationResponse>) : ListUiState
        data object Empty : ListUiState
        data class Error(val message: String?) : ListUiState
    }

    data class JoinUiState(
        val codeInput: String = "",
        val joining: Boolean = false,
        val joinError: String? = null,
        val joined: Boolean = false,
    )

    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    private val _joinStates = MutableStateFlow<Map<Long, JoinUiState>>(emptyMap())
    val joinStates: StateFlow<Map<Long, JoinUiState>> = _joinStates.asStateFlow()

    init {
        load()
    }

    fun joinStateFor(invitationId: Long): JoinUiState =
        _joinStates.value[invitationId] ?: JoinUiState()

    fun load() {
        _uiState.value = ListUiState.Loading
        viewModelScope.launch {
            try {
                val invitations = getMyEmailInvitationsUseCase()
                _uiState.value = if (invitations.isEmpty()) {
                    ListUiState.Empty
                } else {
                    ListUiState.Invitations(invitations)
                }
            } catch (e: Exception) {
                _uiState.value = ListUiState.Error(e.message)
            }
        }
    }

    fun retry() = load()

    fun onCodeChange(invitationId: Long, value: String) {
        _joinStates.update { states ->
            val current = states[invitationId] ?: JoinUiState()
            states + (invitationId to current.copy(codeInput = value, joinError = null))
        }
    }

    fun join(invitationId: Long) {
        val current = joinStateFor(invitationId)
        val code = current.codeInput.trim()
        if (code.isBlank()) return

        _joinStates.update { states ->
            states + (invitationId to current.copy(joining = true, joinError = null))
        }
        viewModelScope.launch {
            try {
                joinViaEmailInviteUseCase(code)
                try { resolveCurrentHouseUseCase() } catch (_: Exception) { }
                _joinStates.update { states ->
                    val state = states[invitationId] ?: JoinUiState()
                    states + (invitationId to state.copy(joining = false, joined = true, joinError = null))
                }
            } catch (e: Exception) {
                _joinStates.update { states ->
                    val state = states[invitationId] ?: JoinUiState()
                    states + (invitationId to state.copy(
                        joining = false,
                        joinError = e.message
                            ?: "Code is invalid or expired. Ask your admin to generate a new one.",
                    ))
                }
            }
        }
    }
}
