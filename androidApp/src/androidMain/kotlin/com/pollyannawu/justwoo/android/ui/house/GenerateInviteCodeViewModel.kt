package com.pollyannawu.justwoo.android.ui.house

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.dto.InviteCodeResponse
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GenerateInviteCodeUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ObserveHouseMembersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GenerateInviteCodeViewModel(
    private val generateInviteCodeUseCase: GenerateInviteCodeUseCase,
    observeCurrentHouseId: ObserveCurrentHouseIdUseCase,
    observeCurrentUserId: ObserveCurrentUserIdUseCase,
    observeHouseMembers: ObserveHouseMembersUseCase,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val generatedCode: InviteCodeResponse? = null,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val currentHouseId = observeCurrentHouseId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isAdmin: StateFlow<Boolean> = combine(
        observeCurrentUserId(),
        currentHouseId,
    ) { userId, houseId -> Pair(userId, houseId) }
        .flatMapLatest { (userId, houseId) ->
            if (userId == null || houseId == null) flowOf(false)
            else observeHouseMembers(houseId).map { members ->
                members.firstOrNull { it.userId == userId }?.role == MemberRole.ADMIN
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun generateCode() {
        val houseId = currentHouseId.value ?: return
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val response = generateInviteCodeUseCase(houseId)
                _uiState.update { it.copy(loading = false, generatedCode = response) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, error = e.message ?: "Failed to generate code.")
                }
            }
        }
    }
}
