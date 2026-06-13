package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.core.dto.BalanceEntry
import com.pollyannawu.justwoo.core.dto.HouseBalanceResponse
import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.model.ApiResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetHouseBalanceUseCaseTest {

    private fun makeUseCase(houseId: Long? = 1L, repo: FakeSettlementRepository = FakeSettlementRepository()) =
        GetHouseBalanceUseCase(repo, GetCurrentHouseIdUseCase(StubAuthRepository(houseId))) to repo

    @Test
    fun `no active house returns Error`() = runTest {
        val (useCase, _) = makeUseCase(houseId = null)
        val result = useCase()
        assertTrue(result is ApiResult.Error)
        assertEquals("No active house", (result as ApiResult.Error).exception.message)
    }

    @Test
    fun `forwards repository Success result`() = runTest {
        val entry = BalanceEntry(
            userId = 1L, userName = "Alice", counterpartId = 2L, counterpartName = "Bob",
            netAmountTwd = 50.0, netAmount = 50.0, currencyCode = "TWD",
        )
        val repo = FakeSettlementRepository(
            balance = ApiResult.Success(HouseBalanceResponse(1L, "TWD", listOf(entry)))
        )
        val (useCase, _) = makeUseCase(repo = repo)
        val result = useCase()
        assertTrue(result is ApiResult.Success)
        assertEquals(listOf(entry), (result as ApiResult.Success).data.balances)
    }

    @Test
    fun `forwards repository Error result`() = runTest {
        val repo = FakeSettlementRepository(balance = ApiResult.Error(Exception("Network error")))
        val (useCase, _) = makeUseCase(repo = repo)
        val result = useCase()
        assertTrue(result is ApiResult.Error)
        assertEquals("Network error", (result as ApiResult.Error).exception.message)
    }

    @Test
    fun `passes correct house id to repository`() = runTest {
        val repo = FakeSettlementRepository()
        val houseId = 42L
        val (useCase, _) = makeUseCase(houseId = houseId, repo = repo)
        val result = useCase()
        assertTrue(result is ApiResult.Success)
    }

    @Test
    fun `currency code is preserved in response`() = runTest {
        val repo = FakeSettlementRepository(
            balance = ApiResult.Success(HouseBalanceResponse(1L, "USD", emptyList()))
        )
        val (useCase, _) = makeUseCase(repo = repo)
        val result = useCase() as ApiResult.Success
        assertEquals("USD", result.data.displayCurrencyCode)
    }
}
