package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.ProfileRepository
import com.pollyannawu.justwoo.backend.repositories.SettlementRepository
import com.pollyannawu.justwoo.backend.utils.dataresult.SettlementDataResult
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
import com.pollyannawu.justwoo.core.dto.CreateSettlementRequest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SettlementServiceTest {

    private val houseRepo: HouseRepository = mockk()
    private val settlementRepo: SettlementRepository = mockk()
    private val profileRepo: ProfileRepository = mockk()

    private val service: SettlementService = DefaultSettlementService(houseRepo, settlementRepo, profileRepo)

    private val now = Clock.System.now()
    private val houseId = 1L
    private val payerId = 10L
    private val payeeId = 20L
    private val requesterId = payerId

    private fun profile(id: Long, name: String) = Profile(
        id = id, name = name, avatar = "", bankAccount = "", bio = "", hashtags = emptyList(), createTime = now, updateTime = now
    )

    private fun fakeSettlement(
        id: Long = 1L,
        amount: Double = 300.0,
        currencyCode: String = "TWD"
    ) = Settlement(
        id = id,
        houseId = houseId,
        payerId = payerId,
        payeeId = payeeId,
        amount = amount,
        currencyCode = currencyCode,
        note = "note",
        createTime = now
    )

    private fun fakeTask(
        ownerId: Long,
        executorId: Long,
        price: Double,
        currencyCode: String
    ): Task {
        return Task(
            id = 99L,
            title = "Task",
            description = "",
            accessLevel = AccessLevel.PUBLIC,
            taskStatus = TaskStatus.DONE,
            ownerId = ownerId,
            executorId = executorId,
            houseId = houseId,
            assignees = listOf(TaskAssignee(executorId, AssignStatus.ACCEPTED)),
            dueTime = now,
            createTime = now,
            updateTime = now,
            price = price,
            currencyCode = currencyCode
        )
    }

    @BeforeEach
    fun setup() {
        coEvery { profileRepo.getProfiles(any()) } returns listOf(
            profile(payerId, "Alice"),
            profile(payeeId, "Bob")
        )
    }

    // ── createSettlement ─────────────────────────────────────────────────────

    @Test
    fun `createSettlement returns Success for valid request`() = runTest {
        val request = CreateSettlementRequest(payerId, payeeId, 300.0, "TWD", "dinner")
        coEvery { houseRepo.isMember(requesterId, houseId) } returns true
        coEvery { settlementRepo.createSettlement(any()) } returns fakeSettlement()

        val result = service.createSettlement(houseId, requesterId, request)

        assertInstanceOf(SettlementDataResult.Success::class.java, result)
        val data = (result as SettlementDataResult.Success).data
        assertEquals(payerId, data.payerId)
        assertEquals(payeeId, data.payeeId)
        assertEquals(300.0, data.amount)
        assertEquals("TWD", data.currencyCode)
    }

    @Test
    fun `createSettlement returns UserNotAllowed for non-member`() = runTest {
        val request = CreateSettlementRequest(payerId, payeeId, 100.0, "TWD")
        coEvery { houseRepo.isMember(requesterId, houseId) } returns false

        val result = service.createSettlement(houseId, requesterId, request)

        assertInstanceOf(SettlementDataResult.Error.UserNotAllowed::class.java, result)
    }

    @Test
    fun `createSettlement returns InvalidAmount for zero amount`() = runTest {
        val request = CreateSettlementRequest(payerId, payeeId, 0.0, "TWD")
        coEvery { houseRepo.isMember(requesterId, houseId) } returns true

        val result = service.createSettlement(houseId, requesterId, request)

        assertInstanceOf(SettlementDataResult.Error.InvalidAmount::class.java, result)
    }

    @Test
    fun `createSettlement returns InvalidAmount for negative amount`() = runTest {
        val request = CreateSettlementRequest(payerId, payeeId, -50.0, "TWD")
        coEvery { houseRepo.isMember(requesterId, houseId) } returns true

        val result = service.createSettlement(houseId, requesterId, request)

        assertInstanceOf(SettlementDataResult.Error.InvalidAmount::class.java, result)
    }

    @Test
    fun `createSettlement returns InvalidCurrency for malformed code`() = runTest {
        val request = CreateSettlementRequest(payerId, payeeId, 100.0, "usd")  // lowercase — invalid format
        coEvery { houseRepo.isMember(requesterId, houseId) } returns true

        val result = service.createSettlement(houseId, requesterId, request)

        assertInstanceOf(SettlementDataResult.Error.InvalidCurrency::class.java, result)
        assertEquals("usd", (result as SettlementDataResult.Error.InvalidCurrency).code)
    }

    @Test
    fun `createSettlement accepts USD`() = runTest {
        val request = CreateSettlementRequest(payerId, payeeId, 10.0, "USD")
        coEvery { houseRepo.isMember(requesterId, houseId) } returns true
        coEvery { settlementRepo.createSettlement(any()) } returns fakeSettlement(amount = 10.0, currencyCode = "USD")

        val result = service.createSettlement(houseId, requesterId, request)

        assertInstanceOf(SettlementDataResult.Success::class.java, result)
        assertEquals("USD", (result as SettlementDataResult.Success).data.currencyCode)
    }

    // ── getSettlements ────────────────────────────────────────────────────────

    @Test
    fun `getSettlements returns list for member`() = runTest {
        coEvery { houseRepo.isMember(requesterId, houseId) } returns true
        coEvery { settlementRepo.getSettlements(houseId) } returns listOf(fakeSettlement(), fakeSettlement(id = 2L))

        val result = service.getSettlements(houseId, requesterId)

        assertInstanceOf(SettlementDataResult.Success::class.java, result)
        assertEquals(2, (result as SettlementDataResult.Success).data.size)
    }

    @Test
    fun `getSettlements returns UserNotAllowed for non-member`() = runTest {
        coEvery { houseRepo.isMember(requesterId, houseId) } returns false

        val result = service.getSettlements(houseId, requesterId)

        assertInstanceOf(SettlementDataResult.Error.UserNotAllowed::class.java, result)
    }

    // ── getHouseBalance ───────────────────────────────────────────────────────

    @Test
    fun `getHouseBalance returns UserNotAllowed for non-member`() = runTest {
        coEvery { houseRepo.isMember(requesterId, houseId) } returns false

        val result = service.getHouseBalance(houseId, requesterId)

        assertInstanceOf(SettlementDataResult.Error.UserNotAllowed::class.java, result)
    }

    @Test
    fun `getHouseBalance shows executor owes owner for task with price`() = runTest {
        // payeeId (Bob) owns the task, payerId (Alice) is executor → Alice owes Bob 320 TWD
        val task = fakeTask(ownerId = payeeId, executorId = payerId, price = 320.0, currencyCode = "TWD")

        coEvery { houseRepo.isMember(requesterId, houseId) } returns true
        coEvery { settlementRepo.getTasksWithPrice(houseId) } returns listOf(task)
        coEvery { settlementRepo.getSettlements(houseId) } returns emptyList()

        val result = service.getHouseBalance(houseId, requesterId)

        assertInstanceOf(SettlementDataResult.Success::class.java, result)
        val balance = (result as SettlementDataResult.Success).data
        assertEquals(1, balance.balances.size)
        val entry = balance.balances.first()
        assertEquals(payerId, entry.userId)        // Alice (debtor)
        assertEquals(payeeId, entry.counterpartId) // Bob (creditor)
        assertEquals("TWD", entry.currencyCode)
        assertEquals(320.0, entry.netAmount, 0.01)
    }

    @Test
    fun `getHouseBalance deducts settlement from debt`() = runTest {
        // Alice owes Bob 320 TWD from task, then Alice pays Bob 100 TWD → net 220 TWD
        val task = fakeTask(ownerId = payeeId, executorId = payerId, price = 320.0, currencyCode = "TWD")
        val settlement = fakeSettlement(amount = 100.0, currencyCode = "TWD")

        coEvery { houseRepo.isMember(requesterId, houseId) } returns true
        coEvery { settlementRepo.getTasksWithPrice(houseId) } returns listOf(task)
        coEvery { settlementRepo.getSettlements(houseId) } returns listOf(settlement)

        val result = service.getHouseBalance(houseId, requesterId)

        assertInstanceOf(SettlementDataResult.Success::class.java, result)
        val entry = (result as SettlementDataResult.Success).data.balances.first()
        assertEquals(220.0, entry.netAmount, 0.01)
    }

    @Test
    fun `getHouseBalance returns empty balances when fully settled`() = runTest {
        val task = fakeTask(ownerId = payeeId, executorId = payerId, price = 100.0, currencyCode = "TWD")
        val settlement = fakeSettlement(amount = 100.0, currencyCode = "TWD")

        coEvery { houseRepo.isMember(requesterId, houseId) } returns true
        coEvery { settlementRepo.getTasksWithPrice(houseId) } returns listOf(task)
        coEvery { settlementRepo.getSettlements(houseId) } returns listOf(settlement)

        val result = service.getHouseBalance(houseId, requesterId)

        assertInstanceOf(SettlementDataResult.Success::class.java, result)
        assertTrue((result as SettlementDataResult.Success).data.balances.isEmpty())
    }

    @Test
    fun `getHouseBalance preserves original currency per entry`() = runTest {
        val task = fakeTask(ownerId = payeeId, executorId = payerId, price = 100.0, currencyCode = "USD")

        coEvery { houseRepo.isMember(requesterId, houseId) } returns true
        coEvery { settlementRepo.getTasksWithPrice(houseId) } returns listOf(task)
        coEvery { settlementRepo.getSettlements(houseId) } returns emptyList()

        val result = service.getHouseBalance(houseId, requesterId)

        assertInstanceOf(SettlementDataResult.Success::class.java, result)
        val entry = (result as SettlementDataResult.Success).data.balances.first()
        assertEquals("USD", entry.currencyCode)
        assertEquals(100.0, entry.netAmount, 0.01)
    }

    @Test
    fun `getHouseBalance skips tasks without executorId`() = runTest {
        val taskNoExecutor = Task(
            id = 1L,
            title = "No executor",
            description = "",
            accessLevel = AccessLevel.PUBLIC,
            taskStatus = TaskStatus.TODO,
            ownerId = payeeId,
            executorId = 0L,
            houseId = houseId,
            assignees = emptyList(),
            dueTime = now,
            createTime = now,
            updateTime = now,
            price = 500.0,
            currencyCode = "TWD"
        )

        coEvery { houseRepo.isMember(requesterId, houseId) } returns true
        coEvery { settlementRepo.getTasksWithPrice(houseId) } returns listOf(taskNoExecutor)
        coEvery { settlementRepo.getSettlements(houseId) } returns emptyList()

        val result = service.getHouseBalance(houseId, requesterId)

        assertInstanceOf(SettlementDataResult.Success::class.java, result)
        assertTrue((result as SettlementDataResult.Success).data.balances.isEmpty())
    }
}
