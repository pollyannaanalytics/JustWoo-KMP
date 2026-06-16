package com.pollyannawu.justwoo.android.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.domain.usecase.settlement.LoadEditExpenseResult
import com.pollyannawu.justwoo.domain.usecase.settlement.LoadEditExpenseUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.SaveExpenseResult
import com.pollyannawu.justwoo.domain.usecase.settlement.SaveExpenseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditExpenseViewModel(
    private val loadEditExpense: LoadEditExpenseUseCase,
    private val saveExpense: SaveExpenseUseCase,
) : ViewModel() {

    data class UiState(
        val amount: String = "",
        val currencyCode: String = "TWD",
        val selectedPayerId: Long? = null,
        val selectedPayeeIds: Set<Long> = emptySet(),
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
        val effectivePayeeIds: Set<Long> get() = (selectedPayeeIds - (selectedPayerId ?: -1L))
        val canSubmit: Boolean get() {
            val av = amountValue
            if (av == null || av <= 0 || isLoading || selectedPayerId == null) return false
            return if (isEditing) effectivePayeeIds.isNotEmpty() else true
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun bind(settlementId: Long?) {
        viewModelScope.launch {
            when (val result = loadEditExpense(settlementId)) {
                is LoadEditExpenseResult.Failure ->
                    _uiState.update { it.copy(error = result.message) }

                is LoadEditExpenseResult.Success -> {
                    val data = result.data
                    val settlement = data.existingSettlement
                    if (settlement != null) {
                        _uiState.update {
                            it.copy(
                                isEditing = true,
                                editingSettlementId = settlementId,
                                amount = settlement.amount.toBigDecimal().stripTrailingZeros().toPlainString(),
                                currencyCode = settlement.currencyCode,
                                note = settlement.note,
                                currentUserId = data.currentUserId,
                                selectedPayerId = settlement.payerId,
                                selectedPayeeIds = setOf(settlement.payeeId),
                                allMembers = data.allMembers,
                                payeeMembers = data.allMembers,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                currentUserId = data.currentUserId,
                                selectedPayerId = data.currentUserId,
                                allMembers = data.allMembers,
                                payeeMembers = data.allMembers,
                            )
                        }
                    }
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
    fun onNoteChange(v: String) = _uiState.update { it.copy(note = v) }

    fun consumeSaved() = _uiState.update { it.copy(saved = false) }

    fun submit() {
        val s = _uiState.value
        val amount = s.amountValue ?: return
        if (amount <= 0) {
            _uiState.update { it.copy(error = "Amount must be greater than zero") }
            return
        }
        val payerId = s.selectedPayerId ?: run {
            _uiState.update { it.copy(error = "Not signed in") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null, partialFailureIds = emptyList()) }
        viewModelScope.launch {
            val saveResult = if (s.isEditing) {
                val settlementId = s.editingSettlementId
                if (settlementId == null) {
                    SaveExpenseResult.Failure("Missing settlement ID")
                } else {
                    saveExpense.update(
                        settlementId = settlementId,
                        payerId = payerId,
                        payeeIds = s.effectivePayeeIds.toList(),
                        amount = amount,
                        currencyCode = s.currencyCode,
                        note = s.note,
                    )
                }
            } else {
                saveExpense.create(
                    payerId = payerId,
                    payeeIds = s.selectedPayeeIds,
                    amount = amount,
                    currencyCode = s.currencyCode,
                    note = s.note,
                )
            }

            when (saveResult) {
                SaveExpenseResult.Success ->
                    if (s.isEditing) {
                        _uiState.update { it.copy(isLoading = false, saved = true) }
                    } else {
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
                    }
                is SaveExpenseResult.PartialFailure ->
                    _uiState.update { it.copy(isLoading = false, partialFailureIds = saveResult.failedPayeeIds) }
                is SaveExpenseResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, error = saveResult.message) }
            }
        }
    }
}
