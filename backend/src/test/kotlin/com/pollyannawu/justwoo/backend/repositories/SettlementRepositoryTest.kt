package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Settlement
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettlementRepositoryTest : BaseRepositoryTest() {

    private val repository: SettlementRepository = DefaultSettlementRepository()
    private val taskRepository: TaskRepository = DefaultTaskRepository()

    private fun buildSettlement(houseId: Long, payerId: Long, payeeId: Long, amount: Double, currencyCode: String = "TWD") =
        Settlement(
            houseId = houseId,
            payerId = payerId,
            payeeId = payeeId,
            amount = amount,
            currencyCode = currencyCode,
            note = "test settlement",
            createTime = now()
        )

    private fun buildTaskWithPrice(ownerId: Long, executorId: Long, houseId: Long, price: Double, currencyCode: String): Task {
        val now = now()
        return Task(
            title = "Priced Task",
            description = "",
            accessLevel = AccessLevel.PUBLIC,
            taskStatus = TaskStatus.TODO,
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

    @Test
    fun `createSettlement persists and returns settlement`() = runTest {
        val payer = insertUser("s-payer@test.com")
        val payee = insertUser("s-payee@test.com")

        val settlement = buildSettlement(houseId = 1L, payerId = payer, payeeId = payee, amount = 300.0)
        val created = repository.createSettlement(settlement)

        assertTrue(created.id > 0)
        assertEquals(payer, created.payerId)
        assertEquals(payee, created.payeeId)
        assertEquals(300.0, created.amount)
        assertEquals("TWD", created.currencyCode)
        assertEquals("test settlement", created.note)
    }

    @Test
    fun `createSettlement stores different currency codes`() = runTest {
        val payer = insertUser("s-usd-payer@test.com")
        val payee = insertUser("s-usd-payee@test.com")

        val settlement = buildSettlement(houseId = 2L, payerId = payer, payeeId = payee, amount = 10.0, currencyCode = "USD")
        val created = repository.createSettlement(settlement)

        assertEquals("USD", created.currencyCode)
        assertEquals(10.0, created.amount)
    }

    @Test
    fun `getSettlements returns all settlements for a house`() = runTest {
        val u1 = insertUser("gs-u1@test.com")
        val u2 = insertUser("gs-u2@test.com")
        val u3 = insertUser("gs-u3@test.com")

        repository.createSettlement(buildSettlement(houseId = 10L, payerId = u1, payeeId = u2, amount = 100.0))
        repository.createSettlement(buildSettlement(houseId = 10L, payerId = u2, payeeId = u3, amount = 200.0))
        repository.createSettlement(buildSettlement(houseId = 99L, payerId = u1, payeeId = u3, amount = 50.0))

        val result = repository.getSettlements(houseId = 10L)

        assertEquals(2, result.size)
        assertTrue(result.all { it.houseId == 10L })
    }

    @Test
    fun `getSettlements returns empty list when no settlements exist`() = runTest {
        val result = repository.getSettlements(houseId = 999L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTasksWithPrice returns only tasks that have a price`() = runTest {
        val owner = insertUser("twp-owner@test.com")
        val executor = insertUser("twp-executor@test.com")

        val taskWithPrice = buildTaskWithPrice(owner, executor, houseId = 20L, price = 500.0, currencyCode = "TWD")
        val now = now()
        val taskWithoutPrice = Task(
            title = "Free Task",
            description = "",
            accessLevel = AccessLevel.PUBLIC,
            taskStatus = TaskStatus.TODO,
            ownerId = owner,
            houseId = 20L,
            assignees = listOf(TaskAssignee(executor, AssignStatus.UNASSIGNED)),
            dueTime = now,
            createTime = now,
            updateTime = now
        )

        taskRepository.createTask(taskWithPrice)
        taskRepository.createTask(taskWithoutPrice)

        val result = repository.getTasksWithPrice(houseId = 20L)

        assertEquals(1, result.size)
        assertEquals(500.0, result.first().price)
        assertEquals("TWD", result.first().currencyCode)
    }

    @Test
    fun `getTasksWithPrice returns empty list when no priced tasks exist`() = runTest {
        val result = repository.getTasksWithPrice(houseId = 999L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getSettlements returns settlements ordered by createTime`() = runTest {
        val u1 = insertUser("order-u1@test.com")
        val u2 = insertUser("order-u2@test.com")

        val s1 = repository.createSettlement(buildSettlement(houseId = 30L, payerId = u1, payeeId = u2, amount = 100.0))
        val s2 = repository.createSettlement(buildSettlement(houseId = 30L, payerId = u2, payeeId = u1, amount = 50.0))

        val result = repository.getSettlements(houseId = 30L)
        assertEquals(s1.id, result[0].id)
        assertEquals(s2.id, result[1].id)
    }
}
