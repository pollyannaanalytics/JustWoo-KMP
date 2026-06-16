package com.pollyannawu.justwoo.android.ui.settlement

import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.core.MemberRole
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.domain.usecase.settlement.EditExpenseInitData
import com.pollyannawu.justwoo.domain.usecase.settlement.LoadEditExpenseResult
import com.pollyannawu.justwoo.domain.usecase.settlement.LoadEditExpenseUseCase
import com.pollyannawu.justwoo.domain.usecase.settlement.SaveExpenseResult
import com.pollyannawu.justwoo.domain.usecase.settlement.SaveExpenseUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class EditExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val loadEditExpense: LoadEditExpenseUseCase = mockk()
    private val saveExpense: SaveExpenseUseCase = mockk()

    private val currentUser = HouseMember(houseId = 1L, userId = 1L, name = "Alice", role = MemberRole.ADMIN, joinedAt = Clock.System.now())
    private val member = HouseMember(houseId = 1L, userId = 2L, name = "Bob", role = MemberRole.MEMBER, joinedAt = Clock.System.now())

    private val defaultInitData = EditExpenseInitData(
        currentUserId = 1L,
        allMembers = listOf(currentUser, member),
        existingSettlement = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { loadEditExpense(null) } returns LoadEditExpenseResult.Success(defaultInitData)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm() = EditExpenseViewModel(loadEditExpense, saveExpense)

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
        coEvery { saveExpense.create(any(), any(), any(), any(), any()) } returns SaveExpenseResult.Success
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
        coEvery { saveExpense.create(any(), emptySet(), any(), any(), any()) } returns SaveExpenseResult.PartialFailure(listOf(2L))
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
        coEvery { saveExpense.create(any(), any(), any(), any(), any()) } returns SaveExpenseResult.Failure("some error")
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
        coEvery { saveExpense.create(any(), any(), any(), any(), any()) } returns SaveExpenseResult.Failure("Amount must be greater than zero")
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
        coEvery { saveExpense.create(any(), any(), any(), any(), any()) } returns SaveExpenseResult.Success
        val vm = buildVm()
        vm.bind(null)
        vm.onAmountChange("50")
        vm.onPayeeToggle(2L)
        advanceUntilIdle()
        vm.submit()
        assertTrue(vm.uiState.value.isLoading)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `consumeSaved resets saved flag`() = runTest {
        coEvery { saveExpense.create(any(), any(), any(), any(), any()) } returns SaveExpenseResult.Success
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
    fun `submit passes selectedPayerId to saveExpense`() = runTest {
        var capturedPayerId: Long? = null
        coEvery { saveExpense.create(any(), any(), any(), any(), any()) } answers {
            capturedPayerId = firstArg()
            SaveExpenseResult.Success
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
    fun `submit passes selectedPayeeIds to saveExpense`() = runTest {
        var capturedPayeeIds: Set<Long>? = null
        coEvery { saveExpense.create(any(), any(), any(), any(), any()) } answers {
            @Suppress("UNCHECKED_CAST")
            capturedPayeeIds = secondArg<Set<Long>>()
            SaveExpenseResult.Success
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

    private fun stubEditLoad() {
        coEvery { loadEditExpense(5L) } returns LoadEditExpenseResult.Success(
            defaultInitData.copy(existingSettlement = editingSettlement)
        )
    }

    @Test
    fun `bind with settlementId loads settlement into state`() = runTest {
        stubEditLoad()
        val vm = buildVm()
        vm.bind(5L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.isEditing)
        assertEquals(5L, state.editingSettlementId)
        assertEquals("250", state.amount)
        assertEquals("USD", state.currencyCode)
        assertEquals("dinner", state.note)
        assertEquals(1L, state.selectedPayerId)
        assertEquals(setOf(2L), state.selectedPayeeIds)
    }

    @Test
    fun `canSubmit is false in edit mode when all selected payees equal payer`() = runTest {
        stubEditLoad()
        val vm = buildVm()
        vm.bind(5L)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.canSubmit)

        vm.onPayeeToggle(2L)
        vm.onPayeeToggle(1L)
        assertFalse(vm.uiState.value.canSubmit)
    }

    @Test
    fun `submit in edit mode calls saveExpense update and sets saved`() = runTest {
        stubEditLoad()
        coEvery { saveExpense.update(any(), any(), any(), any(), any(), any()) } returns SaveExpenseResult.Success
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
        stubEditLoad()
        coEvery { saveExpense.update(any(), any(), any(), any(), any(), any()) } returns SaveExpenseResult.Failure("Forbidden")
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
    fun `onPayeeToggle adds and removes payees in edit mode`() = runTest {
        stubEditLoad()
        val vm = buildVm()
        vm.bind(5L)
        advanceUntilIdle()
        assertEquals(setOf(2L), vm.uiState.value.selectedPayeeIds)

        vm.onPayeeToggle(3L)
        assertEquals(setOf(2L, 3L), vm.uiState.value.selectedPayeeIds)

        vm.onPayeeToggle(2L)
        assertEquals(setOf(3L), vm.uiState.value.selectedPayeeIds)
    }
}
