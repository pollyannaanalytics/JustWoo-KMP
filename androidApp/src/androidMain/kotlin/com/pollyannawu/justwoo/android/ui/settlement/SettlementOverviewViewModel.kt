package com.pollyannawu.justwoo.android.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.dto.BalanceEntry
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import kotlinx.coroutines.flow.first
import com.pollyannawu.justwoo.domain.usecase.settlement.GetHouseBalanceUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.ObserveSettlementsUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.SyncSettlementsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettlementOverviewViewModel(
    private val observeSettlements: ObserveSettlementsUseCase,
    private val syncSettlements: SyncSettlementsUseCase,
    private val getHouseBalance: GetHouseBalanceUseCase,
    private val observeCurrentUserId: ObserveCurrentUserIdUseCase,
) : ViewModel() {

    data class CurrencySummary(val currencyCode: String, val amount: Double)

    data class UiState(
        val settlements: List<Settlement> = emptyList(),
        val balanceEntries: List<BalanceEntry> = emptyList(),
        val isBalanceLoading: Boolean = true,
        val balanceError: String? = null,
        val oweSummary: List<CurrencySummary> = emptyList(),
        val owedSummary: List<CurrencySummary> = emptyList(),
        val currentUserId: Long? = null,
    )

    private val _isBalanceLoading = MutableStateFlow(true)
    private val _balanceError = MutableStateFlow<String?>(null)
    private val _balanceEntries = MutableStateFlow<List<BalanceEntry>>(emptyList())
    private val _currentUserId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<UiState> = combine(
        observeSettlements(),
        _balanceEntries,
        _isBalanceLoading,
        _balanceError,
        _currentUserId,
    ) { settlements, entries, loading, error, currentUserId ->
        UiState(
            settlements = settlements,
            balanceEntries = entries,
            isBalanceLoading = loading,
            balanceError = error,
            currentUserId = currentUserId,
            oweSummary = entries
                .filter { it.netAmount > 0.0 }
                .groupBy { it.currencyCode }
                .map { (code, list) -> CurrencySummary(code, list.sumOf { it.netAmount }) },
            owedSummary = entries
                .filter { it.netAmount < 0.0 }
                .groupBy { it.currencyCode }
                .map { (code, list) -> CurrencySummary(code, list.sumOf { -it.netAmount }) },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    init {
        loadBalance()
    }

    fun refresh() {
        viewModelScope.launch { syncSettlements() }
        loadBalance()
    }

    private fun loadBalance() {
        _isBalanceLoading.value = true
        _balanceError.value = null
        viewModelScope.launch {
            getHouseBalance().fold(
                onSuccess = { data ->
                    val currentUserId = observeCurrentUserId().first()
                        ?: run { _isBalanceLoading.value = false; return@launch }
                    _currentUserId.value = currentUserId
                    val debts = data.balances.filter { it.userId == currentUserId }
                    val owed = data.balances
                        .filter { it.counterpartId == currentUserId }
                        .map { entry ->
                            entry.copy(
                                userId = currentUserId,
                                userName = entry.counterpartName,
                                counterpartId = entry.userId,
                                counterpartName = entry.userName,
                                netAmount = -entry.netAmount,
                            )
                        }
                    _balanceEntries.value = (debts + owed).filter { it.userId != it.counterpartId }
                    _isBalanceLoading.value = false
                },
                onFailure = { e ->
                    _balanceError.update { e.message ?: "Failed to load balance" }
                    _isBalanceLoading.value = false
                },
            )
        }
    }
}
