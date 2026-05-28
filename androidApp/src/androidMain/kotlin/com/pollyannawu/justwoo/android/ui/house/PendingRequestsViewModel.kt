package com.pollyannawu.justwoo.android.ui.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import com.pollyannawu.justwoo.data.HouseInviteRepository
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ApproveMemberUseCase
import com.pollyannawu.justwoo.domain.usecase.house.RejectMemberUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PendingRequestsViewModel(
    private val houseInviteRepository: HouseInviteRepository,
    private val approveMemberUseCase: ApproveMemberUseCase,
    private val rejectMemberUseCase: RejectMemberUseCase,
    observeCurrentHouseId: ObserveCurrentHouseIdUseCase,
) : ViewModel() {

    data class UiState(
        val requests: List<JoinRequestResponse> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Event {
        data class ShowSnackbar(val message: String) : Event
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = Channel<Event>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val currentHouseId = observeCurrentHouseId()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val houseId = currentHouseId.first() ?: run {
                _uiState.update { it.copy(loading = false) }
                return@launch
            }
            try {
                val requests = houseInviteRepository.getPendingRequests(houseId)
                _uiState.update { it.copy(loading = false, requests = requests) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun approve(requestId: Long) = processRequest(requestId, approve = true)
    fun reject(requestId: Long) = processRequest(requestId, approve = false)

    private fun processRequest(requestId: Long, approve: Boolean) {
        viewModelScope.launch {
            try {
                if (approve) approveMemberUseCase(requestId) else rejectMemberUseCase(requestId)
                load()
            } catch (e: Exception) {
                val msg = e.message ?: ""
                if (msg.contains("409") || msg.contains("already", ignoreCase = true)) {
                    _events.send(Event.ShowSnackbar("Request was already handled."))
                    load()
                } else {
                    _uiState.update { it.copy(error = msg) }
                }
            }
        }
    }
}
