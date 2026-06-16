package com.pollyannawu.justwoo.android.ui.house

import com.pollyannawu.justwoo.core.dto.EmailInvitationResponse
import com.pollyannawu.justwoo.domain.usecase.house.CreateEmailInvitationUseCase
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
import kotlin.time.Duration.Companion.days
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InviteByEmailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val useCase: CreateEmailInvitationUseCase = mockk()
    private lateinit var viewModel: InviteByEmailViewModel

    private val fakeResponse = EmailInvitationResponse(
        id = 1L,
        houseId = 10L,
        houseName = "Test House",
        houseAvatar = "",
        code = "ABCD1234",
        expiresAt = Clock.System.now() + 7.days,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = InviteByEmailViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is EnteringEmail with empty email`() {
        val state = viewModel.uiState.value
        assertTrue(state is InviteByEmailViewModel.UiState.EnteringEmail)
        assertEquals("", (state as InviteByEmailViewModel.UiState.EnteringEmail).email)
    }

    @Test
    fun `sendInvite with invalid email shows validation error without network call`() = runTest {
        viewModel.onEmailChange("not-an-email")
        viewModel.sendInvite(houseId = 10L)
        advanceUntilIdle()

        val state = viewModel.uiState.value as InviteByEmailViewModel.UiState.EnteringEmail
        assertNotNull(state.error)
    }

    @Test
    fun `sendInvite with valid email transitions to CodeGenerated on success`() = runTest {
        coEvery { useCase(10L, "friend@example.com") } returns fakeResponse

        viewModel.onEmailChange("friend@example.com")
        viewModel.sendInvite(houseId = 10L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is InviteByEmailViewModel.UiState.CodeGenerated)
        assertEquals("ABCD1234", (state as InviteByEmailViewModel.UiState.CodeGenerated).code)
        assertEquals("Test House", state.houseName)
    }

    @Test
    fun `sendInvite shows error state on API failure`() = runTest {
        coEvery { useCase(10L, "friend@example.com") } throws Exception("Network error")

        viewModel.onEmailChange("friend@example.com")
        viewModel.sendInvite(houseId = 10L)
        advanceUntilIdle()

        val state = viewModel.uiState.value as InviteByEmailViewModel.UiState.EnteringEmail
        assertNotNull(state.error)
        assertNull(null) // not in loading
        assertEquals(false, state.loading)
    }

    @Test
    fun `reset returns to EnteringEmail state`() = runTest {
        coEvery { useCase(10L, "friend@example.com") } returns fakeResponse

        viewModel.onEmailChange("friend@example.com")
        viewModel.sendInvite(houseId = 10L)
        advanceUntilIdle()

        viewModel.reset()

        val state = viewModel.uiState.value
        assertTrue(state is InviteByEmailViewModel.UiState.EnteringEmail)
        assertEquals("", (state as InviteByEmailViewModel.UiState.EnteringEmail).email)
    }
}
