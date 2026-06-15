package com.pollyannawu.justwoo.android.ui.settlement

import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.CreateSettlementResult
import com.pollyannawu.justwoo.domain.usecase.settlement.CreateSettlementUseCase
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
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val createSettlement: CreateSettlementUseCase = mockk()
    private val getHouseMembers: GetHouseMembersUseCase = mockk()
    private val getCurrentHouseId: GetCurrentHouseIdUseCase = mockk()
    private val observeCurrentUserId: ObserveCurrentUserIdUseCase = mockk()

    private val currentUser = HouseMember(houseId = 1L, userId = 1L, name = "Alice", role = MemberRole.ADMIN, joinedAt = Clock.System.now())
    private val member = HouseMember(houseId = 1L, userId = 2L, name = "Bob", role = MemberRole.MEMBER, joinedAt = Clock.System.now())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getCurrentHouseId() } returns 1L
        every { observeCurrentUserId() } returns flowOf(1L)
        coEvery { getHouseMembers(1L) } returns listOf(currentUser, member)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm() = AddExpenseViewModel(createSettlement, getHouseMembers, getCurrentHouseId, observeCurrentUserId)

    @Test
    fun `canSubmit is false when amount is empty`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `canSubmit is false when amount is zero`() = runTest {
        val vm = buildVm()
        vm.onAmountChange("0")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `canSubmit is true when amount is positive`() = runTest {
        val vm = buildVm()
        vm.onAmountChange("100")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canSubmit)
    }

    @Test
    fun `submit with zero amount sets error`() = runTest {
        val vm = buildVm()
        vm.onAmountChange("0")
        advanceUntilIdle()
        vm.submit()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)
    }

    @Test
    fun `submit specific payee succeeds and sets saved`() = runTest {
        coEvery { createSettlement(any(), any(), any(), any(), any()) } returns CreateSettlementResult.Success
        val vm = buildVm()
        vm.onAmountChange("100")
        vm.onPayeeSelect(2L)
        advanceUntilIdle()
        vm.submit()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.saved)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `submit house-wide partial failure sets partialFailureIds`() = runTest {
        coEvery { createSettlement(any(), null, any(), any(), any()) } returns CreateSettlementResult.PartialFailure(listOf(2L))
        val vm = buildVm()
        vm.onAmountChange("90")
        advanceUntilIdle()
        vm.submit()
        advanceUntilIdle()
        assertEquals(listOf(2L), vm.uiState.value.partialFailureIds)
        assertFalse(vm.uiState.value.saved)
    }

    @Test
    fun `onAmountChange filters non-numeric characters`() = runTest {
        val vm = buildVm()
        vm.onAmountChange("a1b2c3")
        advanceUntilIdle()
        assertEquals("123", vm.uiState.value.amount)
    }

    @Test
    fun `onAmountChange allows single decimal point`() = runTest {
        val vm = buildVm()
        vm.onAmountChange("12.50")
        advanceUntilIdle()
        assertEquals("12.50", vm.uiState.value.amount)
    }

    @Test
    fun `onAmountChange clears existing error`() = runTest {
        coEvery { createSettlement(any(), any(), any(), any(), any()) } returns CreateSettlementResult.Failure("some error")
        val vm = buildVm()
        vm.onAmountChange("100")
        vm.onPayeeSelect(2L)
        advanceUntilIdle()
        vm.submit()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.error)

        vm.onAmountChange("200")
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `onCurrencyChange uppercases input`() = runTest {
        val vm = buildVm()
        vm.onCurrencyChange("usd")
        advanceUntilIdle()
        assertEquals("USD", vm.uiState.value.currencyCode)
    }

    @Test
    fun `submit api failure shows error message from result`() = runTest {
        coEvery { createSettlement(any(), any(), any(), any(), any()) } returns CreateSettlementResult.Failure("Amount must be greater than zero")
        val vm = buildVm()
        vm.onAmountChange("333")
        vm.onPayeeSelect(2L)
        advanceUntilIdle()
        vm.submit()
        advanceUntilIdle()
        assertEquals("Amount must be greater than zero", vm.uiState.value.error)
        assertFalse(vm.uiState.value.saved)
    }

    @Test
    fun `isLoading true during submit then cleared on success`() = runTest {
        coEvery { createSettlement(any(), any(), any(), any(), any()) } returns CreateSettlementResult.Success
        val vm = buildVm()
        vm.onAmountChange("50")
        vm.onPayeeSelect(2L)
        advanceUntilIdle()
        vm.submit()
        // loading starts before coroutine finishes
        assertTrue(vm.uiState.value.isLoading)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `consumeSaved resets saved flag`() = runTest {
        coEvery { createSettlement(any(), any(), any(), any(), any()) } returns CreateSettlementResult.Success
        val vm = buildVm()
        vm.onAmountChange("100")
        vm.onPayeeSelect(2L)
        advanceUntilIdle()
        vm.submit()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.saved)

        vm.consumeSaved()
        assertFalse(vm.uiState.value.saved)
    }

    @Test
    fun `selectedPayerId defaults to currentUserId after init`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        assertEquals(1L, vm.uiState.value.selectedPayerId)
    }

    @Test
    fun `allMembers includes all house members after init`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        assertEquals(listOf(currentUser, member), vm.uiState.value.allMembers)
    }

    @Test
    fun `onPayerSelect updates selectedPayerId`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()
        vm.onPayerSelect(2L)
        assertEquals(2L, vm.uiState.value.selectedPayerId)
    }

    @Test
    fun `submit passes selectedPayerId to createSettlement`() = runTest {
        var capturedPayerId: Long? = null
        coEvery { createSettlement(any(), any(), any(), any(), any()) } answers {
            capturedPayerId = firstArg()
            CreateSettlementResult.Success
        }
        val vm = buildVm()
        advanceUntilIdle()
        vm.onPayerSelect(2L)
        vm.onAmountChange("100")
        vm.onPayeeSelect(2L)
        vm.submit()
        advanceUntilIdle()
        assertEquals(2L, capturedPayerId)
    }
}
