package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveSettlementsUseCaseTest {

    private fun makeUseCase(
        houseId: Long? = 1L,
        repo: FakeSettlementRepository = FakeSettlementRepository(),
    ) = ObserveSettlementsUseCase(repo, GetCurrentHouseIdUseCase(StubAuthRepository(houseId)))

    @Test
    fun `no active house returns empty flow`() = runTest {
        val repo = FakeSettlementRepository(settlementsFlow = flowOf(listOf(stubSettlement())))
        val useCase = makeUseCase(houseId = null, repo = repo)
        val emissions = useCase().toList()
        assertTrue(emissions.isEmpty())
    }

    @Test
    fun `has house returns settlements from repository`() = runTest {
        val settlement = stubSettlement()
        val repo = FakeSettlementRepository(settlementsFlow = flowOf(listOf(settlement)))
        val useCase = makeUseCase(repo = repo)
        val result = useCase().first()
        assertEquals(1, result.size)
        assertEquals(settlement, result[0])
    }

    @Test
    fun `has house with empty list returns empty list`() = runTest {
        val repo = FakeSettlementRepository(settlementsFlow = flowOf(emptyList()))
        val useCase = makeUseCase(repo = repo)
        val result = useCase().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `emits multiple settlements`() = runTest {
        val s1 = stubSettlement(id = 1L)
        val s2 = stubSettlement(id = 2L)
        val repo = FakeSettlementRepository(settlementsFlow = flowOf(listOf(s1, s2)))
        val useCase = makeUseCase(repo = repo)
        val result = useCase().first()
        assertEquals(2, result.size)
    }
}
