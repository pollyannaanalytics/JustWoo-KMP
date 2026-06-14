package com.pollyannawu.justwoo.domain.usecase.settlement

import com.pollyannawu.justwoo.domain.usecase.auth.GetCurrentHouseIdUseCase
import com.pollyannawu.justwoo.domain.usecase.house.GetHouseMembersUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateSettlementUseCaseTest {

    private fun makeUseCase(
        houseId: Long? = 1L,
        members: List<com.pollyannawu.justwoo.core.HouseMember> = emptyList(),
        repo: FakeSettlementRepository = FakeSettlementRepository(),
    ): Pair<CreateSettlementUseCase, FakeSettlementRepository> {
        val useCase = CreateSettlementUseCase(
            settlementRepository = repo,
            getCurrentHouseId = GetCurrentHouseIdUseCase(StubAuthRepository(houseId)),
            getHouseMembers = GetHouseMembersUseCase(StubHouseRepository(houseId ?: 0L, members)),
        )
        return useCase to repo
    }

    @Test
    fun `no active house returns Failure`() = runTest {
        val (useCase, _) = makeUseCase(houseId = null)
        val result = useCase(payerId = 1L, payeeId = 2L, amount = 100.0, currencyCode = "TWD", note = "")
        assertTrue(result is CreateSettlementResult.Failure)
        assertEquals("No active house", (result as CreateSettlementResult.Failure).message)
    }

    @Test
    fun `single payee success returns Success`() = runTest {
        val (useCase, _) = makeUseCase()
        val result = useCase(payerId = 1L, payeeId = 2L, amount = 333.0, currencyCode = "TWD", note = "")
        assertTrue(result is CreateSettlementResult.Success)
    }

    @Test
    fun `single payee creates settlement with correct fields`() = runTest {
        val (useCase, repo) = makeUseCase()
        useCase(payerId = 1L, payeeId = 2L, amount = 99.0, currencyCode = "USD", note = "dinner")
        assertEquals(1, repo.created.size)
        val req = repo.created[0]
        assertEquals(1L, req.payerId)
        assertEquals(2L, req.payeeId)
        assertEquals(99.0, req.amount)
        assertEquals("USD", req.currencyCode)
        assertEquals("dinner", req.note)
    }

    @Test
    fun `single payee api error returns Failure with message`() = runTest {
        val repo = FakeSettlementRepository(
            createFn = { _, _ -> Result.failure(Exception("Amount must be greater than zero")) }
        )
        val (useCase, _) = makeUseCase(repo = repo)
        val result = useCase(payerId = 1L, payeeId = 2L, amount = 100.0, currencyCode = "TWD", note = "")
        assertTrue(result is CreateSettlementResult.Failure)
        assertEquals("Amount must be greater than zero", (result as CreateSettlementResult.Failure).message)
    }

    @Test
    fun `house-wide no other members returns Success without creating settlements`() = runTest {
        val repo = FakeSettlementRepository()
        val (useCase, _) = makeUseCase(
            members = listOf(stubMember(userId = 1L)),
            repo = repo,
        )
        val result = useCase(payerId = 1L, payeeId = null, amount = 100.0, currencyCode = "TWD", note = "")
        assertTrue(result is CreateSettlementResult.Success)
        assertTrue(repo.created.isEmpty())
    }

    @Test
    fun `house-wide one other member creates settlement for full amount`() = runTest {
        val repo = FakeSettlementRepository()
        val (useCase, _) = makeUseCase(
            members = listOf(stubMember(1L), stubMember(2L)),
            repo = repo,
        )
        val result = useCase(payerId = 1L, payeeId = null, amount = 90.0, currencyCode = "TWD", note = "")
        assertTrue(result is CreateSettlementResult.Success)
        assertEquals(1, repo.created.size)
        assertEquals(90.0, repo.created[0].amount)
        assertEquals(2L, repo.created[0].payeeId)
    }

    @Test
    fun `house-wide two other members splits amount equally`() = runTest {
        val repo = FakeSettlementRepository()
        val (useCase, _) = makeUseCase(
            members = listOf(stubMember(1L), stubMember(2L), stubMember(3L)),
            repo = repo,
        )
        useCase(payerId = 1L, payeeId = null, amount = 60.0, currencyCode = "TWD", note = "")
        assertEquals(2, repo.created.size)
        assertEquals(30.0, repo.created[0].amount)
        assertEquals(30.0, repo.created[1].amount)
    }

    @Test
    fun `house-wide remainder assigned to last member`() = runTest {
        // 10.0 split 3 ways: 3.33 + 3.33 + 3.34
        val repo = FakeSettlementRepository()
        val (useCase, _) = makeUseCase(
            members = listOf(stubMember(1L), stubMember(2L), stubMember(3L), stubMember(4L)),
            repo = repo,
        )
        useCase(payerId = 1L, payeeId = null, amount = 10.0, currencyCode = "TWD", note = "")
        assertEquals(3, repo.created.size)
        assertEquals(3.33, repo.created[0].amount, 0.005)
        assertEquals(3.33, repo.created[1].amount, 0.005)
        assertEquals(3.34, repo.created[2].amount, 0.005)
    }

    @Test
    fun `house-wide total amount is preserved across members`() = runTest {
        val repo = FakeSettlementRepository()
        val (useCase, _) = makeUseCase(
            members = listOf(stubMember(1L), stubMember(2L), stubMember(3L), stubMember(4L)),
            repo = repo,
        )
        useCase(payerId = 1L, payeeId = null, amount = 10.0, currencyCode = "TWD", note = "")
        val total = repo.created.sumOf { it.amount }
        assertEquals(10.0, total, 0.001)
    }

    @Test
    fun `house-wide partial failure returns PartialFailure with failed member ids`() = runTest {
        var callIndex = 0
        val repo = FakeSettlementRepository(
            createFn = { _, _ ->
                if (callIndex++ == 0) Result.failure(Exception("db error"))
                else Result.success(stubSettlementResponse())
            }
        )
        val (useCase, _) = makeUseCase(
            members = listOf(stubMember(1L), stubMember(2L), stubMember(3L)),
            repo = repo,
        )
        val result = useCase(payerId = 1L, payeeId = null, amount = 20.0, currencyCode = "TWD", note = "")
        assertTrue(result is CreateSettlementResult.PartialFailure)
        assertEquals(listOf(2L), (result as CreateSettlementResult.PartialFailure).failedMemberIds)
    }

    @Test
    fun `house-wide all members fail returns PartialFailure`() = runTest {
        val repo = FakeSettlementRepository(
            createFn = { _, _ -> Result.failure(Exception("error")) }
        )
        val (useCase, _) = makeUseCase(
            members = listOf(stubMember(1L), stubMember(2L), stubMember(3L)),
            repo = repo,
        )
        val result = useCase(payerId = 1L, payeeId = null, amount = 20.0, currencyCode = "TWD", note = "")
        assertTrue(result is CreateSettlementResult.PartialFailure)
        assertEquals(listOf(2L, 3L), (result as CreateSettlementResult.PartialFailure).failedMemberIds)
    }
}
