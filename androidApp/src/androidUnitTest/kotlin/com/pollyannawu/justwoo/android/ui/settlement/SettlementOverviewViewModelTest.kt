package com.pollyannawu.justwoo.android.ui.settlement

import com.pollyannawu.justwoo.core.dto.BalanceEntry
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.GetHouseBalanceUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.ObserveSettlementsUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.SyncSettlementsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
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
    private val observeCurrentUserId: ObserveCurrentUserIdUseCase = mockk()
    private val getHouseMembers: GetHouseMembersUseCase = mockk()
    private val getCurrentHouseId: GetCurrentHouseIdUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeSettlements() } returns flowOf(emptyList())
        every { observeCurrentUserId() } returns flowOf(1L)
        coEvery { syncSettlements() } returns Unit
        coEvery { getCurrentHouseId() } returns 1L
        coEvery { getHouseMembers(any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm(
        getHouseBalance: GetHouseBalanceUseCase,
    ) = SettlementOverviewViewModel(observeSettlements, syncSettlements, getHouseBalance, observeCurrentUserId, getHouseMembers, getCurrentHouseId)

    @Test
    fun `balance loads successfully and filters to current user`() = runTest {
        val entry = BalanceEntry(userId = 1L, userName = "Alice", counterpartId = 2L, counterpartName = "Bob", netAmount = 100.0, currencyCode = "TWD")
        val vm = buildVm { Result.success(HouseBalanceResponse(houseId = 1L, balances = listOf(entry))) }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isBalanceLoading)
        assertNull(state.balanceError)
        assertEquals(1, state.balanceEntries.size)
        assertEquals(entry, state.balanceEntries.first())
    }

    @Test
    fun `balance error sets balanceError and clears loading`() = runTest {
        val vm = buildVm { Result.failure(Exception("Network error")) }
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isBalanceLoading)
        assertNotNull(state.balanceError)
        assertTrue(state.balanceEntries.isEmpty())
    }

    @Test
    fun `settlements history shows empty list when no records`() = runTest {
        val vm = buildVm { Result.success(HouseBalanceResponse(houseId = 1L, balances = emptyList())) }
        advanceUntilIdle()

        assertTrue(vm.uiState.value.settlements.isEmpty())
    }

    @Test
    fun `balance filters out entries belonging to other users`() = runTest {
        val myEntry = BalanceEntry(userId = 1L, userName = "Me", counterpartId = 2L, counterpartName = "Bob", netAmount = 30.0, currencyCode = "TWD")
        val otherEntry = BalanceEntry(userId = 2L, userName = "Bob", counterpartId = 3L, counterpartName = "Carol", netAmount = 10.0, currencyCode = "TWD")
        val vm = buildVm { Result.success(HouseBalanceResponse(houseId = 1L, balances = listOf(myEntry, otherEntry))) }
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.balanceEntries.size)
        assertEquals(1L, vm.uiState.value.balanceEntries[0].userId)
    }

    @Test
    fun `balance error message propagates from exception`() = runTest {
        val vm = buildVm { Result.failure(Exception("Timeout")) }
        advanceUntilIdle()

        assertEquals("Timeout", vm.uiState.value.balanceError)
    }

    @Test
    fun `balance error falls back to default message when exception has no message`() = runTest {
        val vm = buildVm { Result.failure(Exception()) }
        advanceUntilIdle()

        assertEquals("Failed to load balance", vm.uiState.value.balanceError)
    }

    @Test
    fun `refresh triggers second balance load`() = runTest {
        val entryAfterRefresh = BalanceEntry(userId = 1L, userName = "Me", counterpartId = 2L, counterpartName = "Bob", netAmount = 200.0, currencyCode = "TWD")
        val answers = mutableListOf(
            Result.success(HouseBalanceResponse(houseId = 1L, balances = emptyList())),
            Result.success(HouseBalanceResponse(houseId = 1L, balances = listOf(entryAfterRefresh))),
        )
        val vm = buildVm { answers.removeFirst() }
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
        val vm = buildVm { Result.success(HouseBalanceResponse(houseId = 1L, balances = emptyList())) }
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
