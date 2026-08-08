package com.pollyannawu.justwoo.domain.usecase.house

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateEmailInvitationUseCaseTest {

    @Test
    fun `delegates to repository with correct houseId and email`() = runTest {
        val expected = stubEmailInvitationResponse()
        val repo = FakeHouseInviteRepository(createEmailInvitationResult = expected)
        val useCase = CreateEmailInvitationUseCase(repo)

        val result = useCase(houseId = 10L, email = "alice@example.com")

        assertEquals(expected, result)
        assertEquals(listOf(10L to "alice@example.com"), repo.createdInvitations)
    }

    @Test
    fun `propagates repository failure`() = runTest {
        val repo = FakeHouseInviteRepository(createEmailInvitationError = RuntimeException("network error"))
        val useCase = CreateEmailInvitationUseCase(repo)

        assertFailsWith<RuntimeException> { useCase(houseId = 10L, email = "alice@example.com") }
    }
}
