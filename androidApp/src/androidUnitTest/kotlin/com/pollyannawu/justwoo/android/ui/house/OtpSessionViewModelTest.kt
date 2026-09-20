package com.pollyannawu.justwoo.android.ui.house

import com.pollyannawu.justwoo.core.dto.OtpInviteSession
import com.pollyannawu.justwoo.domain.usecase.house.GenerateOtpSessionUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class OtpSessionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val generateOtpSessionUseCase: GenerateOtpSessionUseCase = mockk()
    private lateinit var viewModel: OtpSessionViewModel

    private val stubSession = OtpInviteSession(
        code = "482931",
        displayNumber = 7,
        expiresInSeconds = 60,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = OtpSessionViewModel(generateOtpSessionUseCase)
    }

    @Test
    fun `initial state is Loading`() {
        coEvery { generateOtpSessionUseCase() } returns stubSession
        createViewModel()

        assertTrue(viewModel.uiState.value is OtpSessionViewModel.OtpSessionUiState.Loading)
    }

    @Test
    fun `success - use case returns session transitions to Success with canRefresh false`() = runTest {
        coEvery { generateOtpSessionUseCase() } returns stubSession
        createViewModel()
        // Advance enough to load the session but not complete the full 60s countdown
        advanceTimeBy(500)

        val state = viewModel.uiState.value
        assertTrue(state is OtpSessionViewModel.OtpSessionUiState.Success)
        val success = state as OtpSessionViewModel.OtpSessionUiState.Success
        assertEquals(stubSession, success.session)
        assertTrue(success.remainingSeconds in 0..60)
        // Refresh button is disabled while countdown is running
        assertEquals(false, success.canRefresh)
    }

    @Test
    fun `error - use case throws transitions to Error`() = runTest {
        coEvery { generateOtpSessionUseCase() } throws Exception("Network failure")
        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is OtpSessionViewModel.OtpSessionUiState.Error)
        val error = state as OtpSessionViewModel.OtpSessionUiState.Error
        assertEquals("Network failure", error.message)
    }

    @Test
    fun `refresh is ignored while countdown is running`() = runTest {
        coEvery { generateOtpSessionUseCase() } returns stubSession
        createViewModel()
        // Advance 500ms — session is loaded, countdown is running, canRefresh == false
        advanceTimeBy(500)

        val newSession = stubSession.copy(code = "111222")
        coEvery { generateOtpSessionUseCase() } returns newSession

        // Refresh while canRefresh == false — should be a no-op
        viewModel.refresh()
        advanceTimeBy(500)

        // State should still show the original session
        val state = viewModel.uiState.value as OtpSessionViewModel.OtpSessionUiState.Success
        assertEquals(stubSession, state.session)
    }
}
