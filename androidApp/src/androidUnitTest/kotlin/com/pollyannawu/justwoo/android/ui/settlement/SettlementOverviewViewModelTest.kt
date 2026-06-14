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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant
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

    @Test
    fun `balance filters out entries belonging to other users`() = runTest {
        val myEntry = BalanceEntry(userId = 1L, userName = "Me", counterpartId = 2L, counterpartName = "Bob", netAmountTwd = 30.0, netAmount = 30.0, currencyCode = "TWD")
        val otherEntry = BalanceEntry(userId = 2L, userName = "Bob", counterpartId = 3L, counterpartName = "Carol", netAmountTwd = 10.0, netAmount = 10.0, currencyCode = "TWD")
        coEvery { getHouseBalance() } returns ApiResult.Success(
            HouseBalanceResponse(houseId = 1L, displayCurrencyCode = "TWD", balances = listOf(myEntry, otherEntry))
        )

        val vm = SettlementOverviewViewModel(observeSettlements, syncSettlements, getHouseBalance, observeCurrentUserId)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.balanceEntries.size)
        assertEquals(1L, vm.uiState.value.balanceEntries[0].userId)
    }

    @Test
    fun `balance error message propagates from exception`() = runTest {
        coEvery { getHouseBalance() } returns ApiResult.Error(Exception("Timeout"))

        val vm = SettlementOverviewViewModel(observeSettlements, syncSettlements, getHouseBalance, observeCurrentUserId)
        advanceUntilIdle()

        assertEquals("Timeout", vm.uiState.value.balanceError)
    }

    @Test
    fun `balance error falls back to default message when exception has no message`() = runTest {
        coEvery { getHouseBalance() } returns ApiResult.Error(Exception())

        val vm = SettlementOverviewViewModel(observeSettlements, syncSettlements, getHouseBalance, observeCurrentUserId)
        advanceUntilIdle()

        assertEquals("Failed to load balance", vm.uiState.value.balanceError)
    }

    @Test
    fun `refresh triggers second balance load`() = runTest {
        val entryAfterRefresh = BalanceEntry(userId = 1L, userName = "Me", counterpartId = 2L, counterpartName = "Bob", netAmountTwd = 200.0, netAmount = 200.0, currencyCode = "TWD")
        coEvery { getHouseBalance() } returnsMany listOf(
            ApiResult.Success(HouseBalanceResponse(houseId = 1L, displayCurrencyCode = "TWD", balances = emptyList())),
            ApiResult.Success(HouseBalanceResponse(houseId = 1L, displayCurrencyCode = "TWD", balances = listOf(entryAfterRefresh))),
        )

        val vm = SettlementOverviewViewModel(observeSettlements, syncSettlements, getHouseBalance, observeCurrentUserId)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.balanceEntries.isEmpty())

        vm.refresh()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.balanceEntries.size)
        assertEquals(200.0, vm.uiState.value.balanceEntries[0].netAmount, 0.0)
    }

    @Test
    fun `settlements list updates when repository flow emits`() = runTest {
        val settlementFlow = MutableStateFlow<List<com.pollyannawu.justwoo.core.Settlement>>(emptyList())
        every { observeSettlements() } returns settlementFlow
        coEvery { getHouseBalance() } returns ApiResult.Success(
            HouseBalanceResponse(houseId = 1L, displayCurrencyCode = "TWD", balances = emptyList())
        )

        val vm = SettlementOverviewViewModel(observeSettlements, syncSettlements, getHouseBalance, observeCurrentUserId)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.settlements.isEmpty())

        val newSettlement = com.pollyannawu.justwoo.core.Settlement(
            id = 1L, houseId = 1L, payerId = 1L, payeeId = 2L,
            amount = 50.0, currencyCode = "TWD",
            createTime = Instant.fromEpochMilliseconds(0),
        )
        settlementFlow.value = listOf(newSettlement)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.settlements.size)
        assertEquals(newSettlement, vm.uiState.value.settlements[0])
    }
}
