package com.pollyannawu.justwoo.android.ui.house

import com.pollyannawu.justwoo.core.JoinRequestStatus
import com.pollyannawu.justwoo.core.dto.EmailInvitationResponse
import com.pollyannawu.justwoo.core.dto.JoinRequestResponse
import com.pollyannawu.justwoo.domain.usecase.house.GetMyEmailInvitationsUseCase
import com.pollyannawu.justwoo.domain.usecase.house.JoinViaEmailInviteUseCase
import com.pollyannawu.justwoo.domain.usecase.house.ResolveCurrentHouseUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PendingInvitationsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val getMyEmailInvitationsUseCase: GetMyEmailInvitationsUseCase = mockk()
    private val joinViaEmailInviteUseCase: JoinViaEmailInviteUseCase = mockk()
    private val resolveCurrentHouseUseCase: ResolveCurrentHouseUseCase = mockk()
    private lateinit var viewModel: PendingInvitationsViewModel

    private val invitation = EmailInvitationResponse(
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
        coEvery { resolveCurrentHouseUseCase() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = PendingInvitationsViewModel(
            getMyEmailInvitationsUseCase = getMyEmailInvitationsUseCase,
            joinViaEmailInviteUseCase = joinViaEmailInviteUseCase,
            resolveCurrentHouseUseCase = resolveCurrentHouseUseCase,
        )
    }

    @Test
    fun `initial state is Loading`() {
        coEvery { getMyEmailInvitationsUseCase() } returns listOf(invitation)
        createViewModel()

        assertTrue(viewModel.uiState.value is PendingInvitationsViewModel.ListUiState.Loading)
    }

    @Test
    fun `load with non-empty result transitions to Invitations`() = runTest {
        coEvery { getMyEmailInvitationsUseCase() } returns listOf(invitation)
        createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PendingInvitationsViewModel.ListUiState.Invitations)
        assertEquals(listOf(invitation), (state as PendingInvitationsViewModel.ListUiState.Invitations).invitations)
    }

    @Test
    fun `load with empty result transitions to Empty`() = runTest {
        coEvery { getMyEmailInvitationsUseCase() } returns emptyList()
        createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PendingInvitationsViewModel.ListUiState.Empty)
    }

    @Test
    fun `load failure transitions to Error`() = runTest {
        coEvery { getMyEmailInvitationsUseCase() } throws Exception("Network error")
        createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PendingInvitationsViewModel.ListUiState.Error)
    }

    @Test
    fun `retry reloads after an error`() = runTest {
        coEvery { getMyEmailInvitationsUseCase() } throws Exception("Network error")
        createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PendingInvitationsViewModel.ListUiState.Error)

        coEvery { getMyEmailInvitationsUseCase() } returns listOf(invitation)
        viewModel.retry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PendingInvitationsViewModel.ListUiState.Invitations)
    }

    @Test
    fun `onCodeChange updates code input for the given invitation`() = runTest {
        coEvery { getMyEmailInvitationsUseCase() } returns listOf(invitation)
        createViewModel()
        advanceUntilIdle()

        viewModel.onCodeChange(invitation.id, "WXYZ")

        assertEquals("WXYZ", viewModel.joinStateFor(invitation.id).codeInput)
    }

    @Test
    fun `join with correct code transitions to Approved`() = runTest {
        coEvery { getMyEmailInvitationsUseCase() } returns listOf(invitation)
        val approvedResponse = JoinRequestResponse(
            id = 1L,
            houseId = invitation.houseId,
            userId = 99L,
            status = JoinRequestStatus.APPROVED,
            createdAt = Clock.System.now(),
        )
        coEvery { joinViaEmailInviteUseCase("ABCD1234") } returns approvedResponse
        createViewModel()
        advanceUntilIdle()

        viewModel.onCodeChange(invitation.id, "ABCD1234")
        viewModel.join(invitation.id)
        advanceUntilIdle()

        val joinState = viewModel.joinStateFor(invitation.id)
        assertTrue(joinState.joined)
        assertEquals(null, joinState.joinError)
    }

    @Test
    fun `join with incorrect code shows inline error`() = runTest {
        coEvery { getMyEmailInvitationsUseCase() } returns listOf(invitation)
        coEvery { joinViaEmailInviteUseCase("WRONG") } throws Exception(
            "Code is invalid or expired. Ask your admin to generate a new one."
        )
        createViewModel()
        advanceUntilIdle()

        viewModel.onCodeChange(invitation.id, "WRONG")
        viewModel.join(invitation.id)
        advanceUntilIdle()

        val joinState = viewModel.joinStateFor(invitation.id)
        assertNotNull(joinState.joinError)
        assertTrue(joinState.joinError!!.contains("invalid or expired"))
        assertEquals(false, joinState.joining)
        assertEquals(false, joinState.joined)
    }
}
