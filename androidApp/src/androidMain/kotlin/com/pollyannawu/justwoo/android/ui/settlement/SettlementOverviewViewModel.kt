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
import com.pollyannawu.justwoo.model.ApiResult
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

    data class UiState(
        val settlements: List<Settlement> = emptyList(),
        val balanceEntries: List<BalanceEntry> = emptyList(),
        val isBalanceLoading: Boolean = true,
        val balanceError: String? = null,
    )

    private val _isBalanceLoading = MutableStateFlow(true)
    private val _balanceError = MutableStateFlow<String?>(null)
    private val _balanceEntries = MutableStateFlow<List<BalanceEntry>>(emptyList())

    val uiState: StateFlow<UiState> = combine(
        observeSettlements(),
        _balanceEntries,
        _isBalanceLoading,
        _balanceError,
    ) { settlements, entries, loading, error ->
        UiState(
            settlements = settlements,
            balanceEntries = entries,
            isBalanceLoading = loading,
            balanceError = error,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    init {
        viewModelScope.launch { syncSettlements() }
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
            when (val result = getHouseBalance()) {
                is ApiResult.Success -> {
                    val currentUserId = observeCurrentUserId().first()
                    _balanceEntries.value = result.data.balances.filter { it.userId == currentUserId }
                    _isBalanceLoading.value = false
                }
                is ApiResult.Error -> {
                    _balanceError.update { result.exception.message ?: "Failed to load balance" }
                    _isBalanceLoading.value = false
                }
                ApiResult.Loading -> Unit
            }
        }
    }
}
