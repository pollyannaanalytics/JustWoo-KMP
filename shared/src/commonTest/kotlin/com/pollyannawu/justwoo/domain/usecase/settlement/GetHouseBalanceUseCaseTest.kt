package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.dto.BalanceEntry
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetHouseBalanceUseCaseTest {

    private fun makeUseCase(houseId: Long? = 1L, repo: FakeSettlementRepository = FakeSettlementRepository()) =
        DefaultGetHouseBalanceUseCase(repo, GetCurrentHouseIdUseCase(StubAuthRepository(houseId))) to repo

    @Test
    fun `no active house returns failure`() = runTest {
        val (useCase, _) = makeUseCase(houseId = null)
        val result = useCase()
        assertTrue(result.isFailure)
        assertEquals("No active house", result.exceptionOrNull()?.message)
    }

    @Test
    fun `forwards repository success result`() = runTest {
        val entry = BalanceEntry(
            userId = 1L, userName = "Alice", counterpartId = 2L, counterpartName = "Bob",
            netAmount = 50.0, currencyCode = "TWD",
        )
        val repo = FakeSettlementRepository(
            balance = Result.success(HouseBalanceResponse(1L, listOf(entry)))
        )
        val (useCase, _) = makeUseCase(repo = repo)
        val result = useCase()
        assertTrue(result.isSuccess)
        assertEquals(listOf(entry), result.getOrNull()?.balances)
    }

    @Test
    fun `forwards repository failure result`() = runTest {
        val repo = FakeSettlementRepository(balance = Result.failure(Exception("Network error")))
        val (useCase, _) = makeUseCase(repo = repo)
        val result = useCase()
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `passes correct house id to repository`() = runTest {
        val repo = FakeSettlementRepository()
        val (useCase, _) = makeUseCase(houseId = 42L, repo = repo)
        val result = useCase()
        assertTrue(result.isSuccess)
    }
}
