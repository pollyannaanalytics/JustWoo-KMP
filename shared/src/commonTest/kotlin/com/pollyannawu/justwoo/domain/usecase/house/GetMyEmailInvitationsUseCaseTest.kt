package com.pollyannawu.justwoo.domain.usecase.house

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetMyEmailInvitationsUseCaseTest {

    @Test
    fun `returns list of pending email invitations from repository`() = runTest {
        val invitations = listOf(stubEmailInvitationResponse(id = 1L), stubEmailInvitationResponse(id = 2L))
        val repo = FakeHouseInviteRepository(myEmailInvitations = invitations)
        val useCase = GetMyEmailInvitationsUseCase(repo)

        val result = useCase()

        assertEquals(invitations, result)
        assertEquals(1, repo.getMyEmailInvitationsCallCount)
    }

    @Test
    fun `returns empty list when there are no pending invitations`() = runTest {
        val repo = FakeHouseInviteRepository(myEmailInvitations = emptyList())
        val useCase = GetMyEmailInvitationsUseCase(repo)

        val result = useCase()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `propagates repository failure`() = runTest {
        val repo = FakeHouseInviteRepository(myEmailInvitationsError = RuntimeException("network error"))
        val useCase = GetMyEmailInvitationsUseCase(repo)

        assertFailsWith<RuntimeException> { useCase() }
    }
}
