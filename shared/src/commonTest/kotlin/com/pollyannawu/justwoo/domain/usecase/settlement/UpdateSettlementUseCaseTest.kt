package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateSettlementUseCaseTest {

    private fun makeUseCase(
        houseId: Long? = 1L,
        repo: FakeSettlementRepository = FakeSettlementRepository(),
    ): Pair<UpdateSettlementUseCase, FakeSettlementRepository> {
        val useCase = UpdateSettlementUseCase(
            settlementRepository = repo,
            getCurrentHouseId = GetCurrentHouseIdUseCase(StubAuthRepository(houseId)),
        )
        return useCase to repo
    }

    @Test
    fun `no active house returns Failure`() = runTest {
        val (useCase, _) = makeUseCase(houseId = null)
        val result = useCase(settlementId = 1L, payerId = 1L, payeeId = 2L, amount = 100.0, currencyCode = "TWD", note = "")
        assertTrue(result is UpdateSettlementResult.Failure)
        assertEquals("No active house", (result as UpdateSettlementResult.Failure).message)
    }

    @Test
    fun `success returns Success and forwards request to repository`() = runTest {
        val (useCase, repo) = makeUseCase()
        val result = useCase(settlementId = 5L, payerId = 1L, payeeId = 2L, amount = 250.0, currencyCode = "USD", note = "edited")
        assertTrue(result is UpdateSettlementResult.Success)
        assertEquals(1, repo.updated.size)
        val req = repo.updated[0]
        assertEquals(1L, req.payerId)
        assertEquals(2L, req.payeeId)
        assertEquals(250.0, req.amount)
        assertEquals("USD", req.currencyCode)
        assertEquals("edited", req.note)
    }

    @Test
    fun `api error returns Failure with message`() = runTest {
        val repo = FakeSettlementRepository(
            updateFn = { _, _, _ -> Result.failure(Exception("Forbidden")) }
        )
        val (useCase, _) = makeUseCase(repo = repo)
        val result = useCase(settlementId = 5L, payerId = 1L, payeeId = 2L, amount = 100.0, currencyCode = "TWD", note = "")
        assertTrue(result is UpdateSettlementResult.Failure)
        assertEquals("Forbidden", (result as UpdateSettlementResult.Failure).message)
    }
}
