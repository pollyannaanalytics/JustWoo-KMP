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

    private val member = HouseMember(houseId = 1L, userId = 2L, name = "Bob", role = MemberRole.MEMBER, joinedAt = Clock.System.now())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getCurrentHouseId() } returns 1L
        every { observeCurrentUserId() } returns flowOf(1L)
        coEvery { getHouseMembers(1L) } returns listOf(member)
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
}
