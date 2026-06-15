package com.pollyannawu.justwoo.domain.usecase.settlement

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetSettlementByIdUseCaseTest {

    @Test
    fun `returns settlement from repository`() = runTest {
        val repo = FakeSettlementRepository(settlementById = stubSettlement(id = 7L))
        val useCase = GetSettlementByIdUseCase(repo)
        val result = useCase(7L)
        assertEquals(7L, result?.id)
    }

    @Test
    fun `returns null when not found`() = runTest {
        val repo = FakeSettlementRepository(settlementById = null)
        val useCase = GetSettlementByIdUseCase(repo)
        val result = useCase(99L)
        assertNull(result)
    }
}
