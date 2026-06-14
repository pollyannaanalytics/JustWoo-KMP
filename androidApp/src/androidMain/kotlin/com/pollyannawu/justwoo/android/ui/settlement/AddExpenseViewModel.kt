package com.pollyannawu.justwoo.android.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.CreateSettlementResult
import com.pollyannawu.justwoo.domain.usecase.settlement.CreateSettlementUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val createSettlement: CreateSettlementUseCase,
    private val getHouseMembers: GetHouseMembersUseCase,
    private val getCurrentHouseId: GetCurrentHouseIdUseCase,
    private val observeCurrentUserId: ObserveCurrentUserIdUseCase,
) : ViewModel() {

    data class UiState(
        val amount: String = "",
        val currencyCode: String = "TWD",
        val selectedPayeeId: Long? = null,
        val note: String = "",
        val members: List<HouseMember> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val partialFailureIds: List<Long> = emptyList(),
        val saved: Boolean = false,
    ) {
        val amountValue: Double? get() = amount.toDoubleOrNull()
        val canSubmit: Boolean get() { val av = amountValue; return av != null && av > 0 && !isLoading }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val houseId = getCurrentHouseId() ?: return@launch
            val currentUserId = observeCurrentUserId().first()
            _uiState.update { it.copy(members = getHouseMembers(houseId).filter { m -> m.userId != currentUserId }) }
        }
    }

    fun onAmountChange(v: String) {
        val filtered = v.filter { it.isDigit() || it == '.' }
            .let { s -> if (s.count { it == '.' } > 1) s.dropLastWhile { it == '.' } else s }
        _uiState.update { it.copy(amount = filtered, error = null) }
    }
    fun onCurrencyChange(v: String) = _uiState.update { it.copy(currencyCode = v.uppercase()) }
    fun onPayeeSelect(payeeId: Long?) = _uiState.update { it.copy(selectedPayeeId = payeeId) }
    fun onNoteChange(v: String) = _uiState.update { it.copy(note = v) }

    fun consumeSaved() = _uiState.update { it.copy(saved = false) }

    fun submit() {
        val s = _uiState.value
        val amount = s.amountValue ?: return
        if (amount <= 0) {
            _uiState.update { it.copy(error = "Amount must be greater than zero") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, partialFailureIds = emptyList()) }
        viewModelScope.launch {
            val payerId = observeCurrentUserId().first() ?: run {
                _uiState.update { it.copy(isLoading = false, error = "Not signed in") }
                return@launch
            }
            when (val result = createSettlement(
                payerId = payerId,
                payeeId = s.selectedPayeeId,
                amount = amount,
                currencyCode = s.currencyCode,
                note = s.note,
            )) {
                CreateSettlementResult.Success ->
                    _uiState.update { it.copy(isLoading = false, saved = true) }
                is CreateSettlementResult.PartialFailure ->
                    _uiState.update { it.copy(isLoading = false, partialFailureIds = result.failedMemberIds) }
                is CreateSettlementResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

}
