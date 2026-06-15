package com.pollyannawu.justwoo.android.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.CreateSettlementResult
import com.pollyannawu.justwoo.domain.usecase.settlement.CreateSettlementUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.GetSettlementByIdUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.UpdateSettlementResult
import com.pollyannawu.justwoo.domain.usecase.settlement.UpdateSettlementUseCase
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
    private val getSettlementById: GetSettlementByIdUseCase,
    private val updateSettlement: UpdateSettlementUseCase,
) : ViewModel() {

    data class UiState(
        val amount: String = "",
        val currencyCode: String = "TWD",
        val selectedPayerId: Long? = null,
        val selectedPayeeIds: Set<Long> = emptySet(),
        val selectedPayeeId: Long? = null,
        val note: String = "",
        val allMembers: List<HouseMember> = emptyList(),
        val payeeMembers: List<HouseMember> = emptyList(),
        val currentUserId: Long? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val partialFailureIds: List<Long> = emptyList(),
        val saved: Boolean = false,
        val isEditing: Boolean = false,
        val editingSettlementId: Long? = null,
    ) {
        val amountValue: Double? get() = amount.toDoubleOrNull()
        val canSubmit: Boolean get() {
            val av = amountValue
            if (av == null || av <= 0 || isLoading || selectedPayerId == null) return false
            return if (isEditing) {
                selectedPayeeId != null && selectedPayeeId != selectedPayerId
            } else {
                true
            }
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun bind(settlementId: Long?) {
        viewModelScope.launch {
            val houseId = getCurrentHouseId() ?: return@launch
            val currentUserId = observeCurrentUserId().first()
            val all = getHouseMembers(houseId)
            _uiState.update {
                it.copy(
                    allMembers = all,
                    payeeMembers = all.filter { m -> m.userId != currentUserId },
                    currentUserId = currentUserId,
                    selectedPayerId = currentUserId,
                )
            }

            if (settlementId != null) {
                val settlement = getSettlementById(settlementId) ?: return@launch
                _uiState.update {
                    it.copy(
                        isEditing = true,
                        editingSettlementId = settlementId,
                        amount = settlement.amount.toString(),
                        currencyCode = settlement.currencyCode,
                        note = settlement.note,
                        selectedPayerId = settlement.payerId,
                        selectedPayeeId = settlement.payeeId,
                    )
                }
            }
        }
    }

    fun onAmountChange(v: String) {
        val filtered = v.filter { it.isDigit() || it == '.' }
            .let { s -> if (s.count { it == '.' } > 1) s.dropLastWhile { it == '.' } else s }
        _uiState.update { it.copy(amount = filtered, error = null) }
    }
    fun onCurrencyChange(v: String) = _uiState.update { it.copy(currencyCode = v.uppercase()) }
    fun onPayerSelect(payerId: Long?) = _uiState.update { it.copy(selectedPayerId = payerId) }
    fun onPayeeToggle(payeeId: Long) = _uiState.update {
        val current = it.selectedPayeeIds
        it.copy(selectedPayeeIds = if (payeeId in current) current - payeeId else current + payeeId)
    }
    fun onEditPayeeSelect(payeeId: Long) = _uiState.update { it.copy(selectedPayeeId = payeeId) }
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
            val payerId = s.selectedPayerId ?: observeCurrentUserId().first() ?: run {
                _uiState.update { it.copy(isLoading = false, error = "Not signed in") }
                return@launch
            }

            if (s.isEditing) {
                val settlementId = s.editingSettlementId
                val payeeId = s.selectedPayeeId
                if (settlementId == null || payeeId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Select a payee") }
                    return@launch
                }
                when (val result = updateSettlement(
                    settlementId = settlementId,
                    payerId = payerId,
                    payeeId = payeeId,
                    amount = amount,
                    currencyCode = s.currencyCode,
                    note = s.note,
                )) {
                    UpdateSettlementResult.Success ->
                        _uiState.update { it.copy(isLoading = false, saved = true) }
                    is UpdateSettlementResult.Failure ->
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            } else {
                when (val result = createSettlement(
                    payerId = payerId,
                    payeeIds = s.selectedPayeeIds,
                    amount = amount,
                    currencyCode = s.currencyCode,
                    note = s.note,
                )) {
                    CreateSettlementResult.Success ->
                        _uiState.update { prev ->
                            UiState(
                                allMembers = prev.allMembers,
                                payeeMembers = prev.payeeMembers,
                                currentUserId = prev.currentUserId,
                                selectedPayerId = prev.selectedPayerId,
                                isLoading = false,
                                saved = true,
                            )
                        }
                    is CreateSettlementResult.PartialFailure ->
                        _uiState.update { it.copy(isLoading = false, partialFailureIds = result.failedMemberIds) }
                    is CreateSettlementResult.Failure ->
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

}
