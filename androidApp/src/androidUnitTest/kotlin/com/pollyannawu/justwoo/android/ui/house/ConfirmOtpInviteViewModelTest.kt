package com.pollyannawu.justwoo.android.ui.house

import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ConfirmOtpInviteUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmOtpInviteViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val confirmOtpInviteUseCase: ConfirmOtpInviteUseCase = mockk()
    private val getCurrentHouseIdUseCase: GetCurrentHouseIdUseCase = mockk()
    private lateinit var viewModel: ConfirmOtpInviteViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { getCurrentHouseIdUseCase() } returns 42L
        viewModel = ConfirmOtpInviteViewModel(confirmOtpInviteUseCase, getCurrentHouseIdUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Idle)
    }

    @Test
    fun `loading - state is Loading while use case is in flight`() = runTest {
        coJustRun { confirmOtpInviteUseCase(any(), any(), any()) }

        viewModel.confirm(code = "123456", displayNumber = 42)

        // Before advancing, coroutine is suspended — state should be Loading
        assertTrue(viewModel.uiState.value is ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Loading)
    }

    @Test
    fun `success - use case completes without throwing transitions to Success`() = runTest {
        coJustRun { confirmOtpInviteUseCase(any(), any(), any()) }

        viewModel.confirm(code = "123456", displayNumber = 42)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Success)
    }

    @Test
    fun `failure - use case throws transitions to Error with exception message`() = runTest {
        coEvery { confirmOtpInviteUseCase(any(), any(), any()) } throws Exception("Invalid or expired code")

        viewModel.confirm(code = "000000", displayNumber = 10)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Error)
        val error = state as ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Error
        assertEquals("Invalid or expired code", error.message)
    }

    @Test
    fun `no house - transitions to Error when houseId is null`() = runTest {
        every { getCurrentHouseIdUseCase() } returns null
        viewModel = ConfirmOtpInviteViewModel(confirmOtpInviteUseCase, getCurrentHouseIdUseCase)

        viewModel.confirm(code = "123456", displayNumber = 42)

        val state = viewModel.uiState.value
        assertTrue(state is ConfirmOtpInviteViewModel.ConfirmOtpInviteUiState.Error)
    }
}
