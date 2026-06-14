package com.pollyannawu.justwoo.android.ui.settlement

import com.pollyannawu.justwoo.core.dto.BalanceEntry
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.GetHouseBalanceUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.ObserveSettlementsUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.SyncSettlementsUseCase
import com.pollyannawu.justwoo.model.ApiResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettlementOverviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeSettlements: ObserveSettlementsUseCase = mockk()
    private val syncSettlements: SyncSettlementsUseCase = mockk()
    private val getHouseBalance: GetHouseBalanceUseCase = mockk()
    private val observeCurrentUserId: ObserveCurrentUserIdUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeSettlements() } returns flowOf(emptyList())
        every { observeCurrentUserId() } returns flowOf(1L)
        coEvery { syncSettlements() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `balance loads successfully and filters to current user`() = runTest {
        val entry = BalanceEntry(userId = 1L, userName = "Alice", counterpartId = 2L, counterpartName = "Bob", netAmountTwd = 100.0, netAmount = 100.0, currencyCode = "TWD")
        coEvery { getHouseBalance() } returns ApiResult.Success(
            HouseBalanceResponse(houseId = 1L, displayCurrencyCode = "TWD", balances = listOf(entry))
        )

        val vm = SettlementOverviewViewModel(observeSettlements, syncSettlements, getHouseBalance, observeCurrentUserId)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isBalanceLoading)
        assertNull(state.balanceError)
        assertEquals(1, state.balanceEntries.size)
        assertEquals(entry, state.balanceEntries.first())
    }

    @Test
    fun `balance error sets balanceError and clears loading`() = runTest {
        coEvery { getHouseBalance() } returns ApiResult.Error(Exception("Network error"))

        val vm = SettlementOverviewViewModel(observeSettlements, syncSettlements, getHouseBalance, observeCurrentUserId)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isBalanceLoading)
        assertNotNull(state.balanceError)
        assertTrue(state.balanceEntries.isEmpty())
    }

    @Test
    fun `settlements history shows empty list when no records`() = runTest {
        coEvery { getHouseBalance() } returns ApiResult.Success(
            HouseBalanceResponse(houseId = 1L, displayCurrencyCode = "TWD", balances = emptyList())
        )

        val vm = SettlementOverviewViewModel(observeSettlements, syncSettlements, getHouseBalance, observeCurrentUserId)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.settlements.isEmpty())
    }
}
