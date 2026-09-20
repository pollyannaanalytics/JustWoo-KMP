package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.JoinRequestStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JoinViaEmailInviteUseCaseTest {

    @Test
    fun `delegates to repository submitJoinRequest with the given code`() = runTest {
        val expected = stubJoinRequestResponse(id = 42L, status = JoinRequestStatus.APPROVED)
        val repo = FakeHouseInviteRepository(submitJoinRequestResult = expected)
        val useCase = JoinViaEmailInviteUseCase(repo)

        val result = useCase("EMAIL-CODE-123")

        assertEquals(expected, result)
        assertEquals(listOf("EMAIL-CODE-123"), repo.submittedCodes)
    }

    @Test
    fun `propagates repository failure for invalid or expired code`() = runTest {
        val repo = FakeHouseInviteRepository(
            submitJoinRequestError = RuntimeException("Code is invalid or expired. Ask your admin to generate a new one."),
        )
        val useCase = JoinViaEmailInviteUseCase(repo)

        assertFailsWith<RuntimeException> { useCase("BAD-CODE") }
    }
}
