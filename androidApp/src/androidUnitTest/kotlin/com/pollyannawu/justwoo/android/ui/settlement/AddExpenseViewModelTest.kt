package com.pollyannawu.justwoo.android.ui.settlement

import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.auth.ObserveCurrentUserIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.CreateSettlementResult
import com.pollyannawu.justwoo.domain.usecase.settlement.CreateSettlementUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.GetSettlementByIdUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.UpdateSettlementResult
import com.pollyannawu.justwoo.domain.usecase.settlement.UpdateSettlementUseCase
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
    private val getSettlementById: GetSettlementByIdUseCase = mockk()
    private val updateSettlement: UpdateSettlementUseCase = mockk()

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

    private fun buildVm() = AddExpenseViewModel(
        createSettlement, getHouseMembers, getCurrentHouseId, observeCurrentUserId, getSettlementById, updateSettlement,
    )

    @Test
    fun `canSubmit is false when amount is empty`() = runTest {
        val vm = buildVm()
        vm.bind(null)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `canSubmit is false when amount is zero`() = runTest {
        val vm = buildVm()
        vm.bind(null)
        vm.onAmountChange("0")
        advanceUntilIdle()
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `canSubmit is true when amount is positive`() = runTest {
        val vm = buildVm()
        vm.bind(null)
        vm.onAmountChange("100")
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canSubmit)
    }

    @Test
    fun `submit with zero amount sets error`() = runTest {
        val vm = buildVm()
        vm.bind(null)
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
        vm.bind(null)
        vm.onAmountChange("100")
        vm.onPayeeToggle(2L)
        advanceUntilIdle()
        vm.submit()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.saved)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `submit house-wide partial failure sets partialFailureIds`() = runTest {
        coEvery { createSettlement(any(), emptySet(), any(), any(), any()) } returns CreateSettlementResult.PartialFailure(listOf(2L))
        val vm = buildVm()
        vm.bind(null)
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
        vm.bind(null)
        vm.onAmountChange("a1b2c3")
        advanceUntilIdle()
        assertEquals("123", vm.uiState.value.amount)
    }

    @Test
    fun `onAmountChange allows single decimal point`() = runTest {
        val vm = buildVm()
        vm.bind(null)
        vm.onAmountChange("12.50")
        advanceUntilIdle()
        assertEquals("12.50", vm.uiState.value.amount)
    }

    @Test
    fun `onAmountChange clears existing error`() = runTest {
        coEvery { createSettlement(any(), any(), any(), any(), any()) } returns CreateSettlementResult.Failure("some error")
        val vm = buildVm()
        vm.bind(null)
        vm.onAmountChange("100")
        vm.onPayeeToggle(2L)
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
        vm.bind(null)
        vm.onCurrencyChange("usd")
        advanceUntilIdle()
        assertEquals("USD", vm.uiState.value.currencyCode)
    }

    @Test
    fun `submit api failure shows error message from result`() = runTest {
        coEvery { createSettlement(any(), any(), any(), any(), any()) } returns CreateSettlementResult.Failure("Amount must be greater than zero")
        val vm = buildVm()
        vm.bind(null)
        vm.onAmountChange("333")
        vm.onPayeeToggle(2L)
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
        vm.bind(null)
        vm.onAmountChange("50")
        vm.onPayeeToggle(2L)
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
        vm.bind(null)
        vm.onAmountChange("100")
        vm.onPayeeToggle(2L)
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
        vm.bind(null)
        advanceUntilIdle()
        assertEquals(1L, vm.uiState.value.selectedPayerId)
    }

    @Test
    fun `allMembers includes all house members after init`() = runTest {
        val vm = buildVm()
        vm.bind(null)
        advanceUntilIdle()
        assertEquals(listOf(currentUser, member), vm.uiState.value.allMembers)
    }

    @Test
    fun `onPayerSelect updates selectedPayerId`() = runTest {
        val vm = buildVm()
        vm.bind(null)
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
        vm.bind(null)
        advanceUntilIdle()
        vm.onPayerSelect(2L)
        vm.onAmountChange("100")
        vm.onPayeeToggle(2L)
        vm.submit()
        advanceUntilIdle()
        assertEquals(2L, capturedPayerId)
    }

    @Test
    fun `onPayeeToggle adds and removes payee from selectedPayeeIds`() = runTest {
        val vm = buildVm()
        vm.bind(null)
        advanceUntilIdle()
        vm.onPayeeToggle(2L)
        assertEquals(setOf(2L), vm.uiState.value.selectedPayeeIds)
        vm.onPayeeToggle(2L)
        assertEquals(emptySet<Long>(), vm.uiState.value.selectedPayeeIds)
    }

    @Test
    fun `submit passes selectedPayeeIds to createSettlement`() = runTest {
        var capturedPayeeIds: Set<Long>? = null
        coEvery { createSettlement(any(), any(), any(), any(), any()) } answers {
            @Suppress("UNCHECKED_CAST")
            capturedPayeeIds = secondArg<Set<Long>>()
            CreateSettlementResult.Success
        }
        val vm = buildVm()
        vm.bind(null)
        advanceUntilIdle()
        vm.onAmountChange("100")
        vm.onPayeeToggle(2L)
        vm.submit()
        advanceUntilIdle()
        assertEquals(setOf(2L), capturedPayeeIds)
    }

    // ── edit mode ──────────────────────────────────────────────────────────

    private val editingSettlement = Settlement(
        id = 5L, houseId = 1L, payerId = 1L, payeeId = 2L,
        amount = 250.0, currencyCode = "USD", note = "dinner",
        createTime = Clock.System.now(),
    )

    @Test
    fun `bind with settlementId loads settlement into state`() = runTest {
        coEvery { getSettlementById(5L) } returns editingSettlement
        val vm = buildVm()
        vm.bind(5L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isEditing)
        assertEquals(5L, state.editingSettlementId)
        assertEquals("250.0", state.amount)
        assertEquals("USD", state.currencyCode)
        assertEquals("dinner", state.note)
        assertEquals(1L, state.selectedPayerId)
        assertEquals(2L, state.selectedPayeeId)
    }

    @Test
    fun `canSubmit is false in edit mode when payee equals payer`() = runTest {
        coEvery { getSettlementById(5L) } returns editingSettlement
        val vm = buildVm()
        vm.bind(5L)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canSubmit)

        vm.onEditPayeeSelect(1L)
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `submit in edit mode calls updateSettlement and sets saved`() = runTest {
        coEvery { getSettlementById(5L) } returns editingSettlement
        coEvery { updateSettlement(any(), any(), any(), any(), any(), any()) } returns UpdateSettlementResult.Success
        val vm = buildVm()
        vm.bind(5L)
        advanceUntilIdle()
        vm.onAmountChange("300")
        vm.submit()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.saved)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `submit in edit mode failure sets error`() = runTest {
        coEvery { getSettlementById(5L) } returns editingSettlement
        coEvery { updateSettlement(any(), any(), any(), any(), any(), any()) } returns UpdateSettlementResult.Failure("Forbidden")
        val vm = buildVm()
        vm.bind(5L)
        advanceUntilIdle()
        vm.onAmountChange("300")
        vm.submit()
        advanceUntilIdle()
        assertEquals("Forbidden", vm.uiState.value.error)
        assertFalse(vm.uiState.value.saved)
    }

    @Test
    fun `onEditPayeeSelect updates selectedPayeeId in edit mode`() = runTest {
        coEvery { getSettlementById(5L) } returns editingSettlement
        val vm = buildVm()
        vm.bind(5L)
        advanceUntilIdle()
        vm.onEditPayeeSelect(2L)
        assertEquals(2L, vm.uiState.value.selectedPayeeId)
    }
}
