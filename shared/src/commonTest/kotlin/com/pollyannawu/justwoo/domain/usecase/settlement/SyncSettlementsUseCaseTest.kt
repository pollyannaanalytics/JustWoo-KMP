package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncSettlementsUseCaseTest {

    private fun makeUseCase(
        houseId: Long? = 1L,
        repo: FakeSettlementRepository = FakeSettlementRepository(),
    ) = SyncSettlementsUseCase(repo, GetCurrentHouseIdUseCase(StubAuthRepository(houseId))) to repo

    @Test
    fun `no active house does not call repository`() = runTest {
        val (useCase, repo) = makeUseCase(houseId = null)
        useCase()
        assertTrue(repo.synced.isEmpty())
    }

    @Test
    fun `has house calls repository with correct house id`() = runTest {
        val (useCase, repo) = makeUseCase(houseId = 7L)
        useCase()
        assertEquals(listOf(7L), repo.synced)
    }

    @Test
    fun `calling sync multiple times records each call`() = runTest {
        val (useCase, repo) = makeUseCase(houseId = 1L)
        useCase()
        useCase()
        assertEquals(2, repo.synced.size)
    }
}
