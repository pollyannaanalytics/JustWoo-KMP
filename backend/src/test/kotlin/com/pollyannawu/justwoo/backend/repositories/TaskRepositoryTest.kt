package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TaskRepositoryTest : BaseRepositoryTest() {

    private val repository: TaskRepository = DefaultTaskRepository()

    private fun buildTask(
        ownerId: Long,
        houseId: Long,
        assigneeIds: List<Long> = emptyList(),
        price: Double? = null,
        currencyCode: String? = null
    ): Task {
        val now = now()
        return Task(
            title = "Test Task",
            description = "Description",
            accessLevel = AccessLevel.PUBLIC,
            taskStatus = TaskStatus.TODO,
            ownerId = ownerId,
            houseId = houseId,
            assignees = assigneeIds.map { TaskAssignee(it, AssignStatus.UNASSIGNED) },
            dueTime = now,
            createTime = now,
            updateTime = now,
            price = price,
            currencyCode = currencyCode
        )
    }

    @Test
    fun `createTask persists task and assignees`() = runTest {
        val ownerId = insertUser("owner@test.com")
        val assigneeId = insertUser("assignee@test.com")
        val task = buildTask(ownerId, houseId = 1L, assigneeIds = listOf(assigneeId))

        val created = repository.createTask(task)

        assertTrue(created.id > 0)
        assertEquals("Test Task", created.title)
        assertEquals(1, created.assignees.size)
        assertEquals(assigneeId, created.assignees.first().userId)
        assertEquals(AssignStatus.UNASSIGNED, created.assignees.first().status)
    }

    @Test
    fun `createTask stores price and currencyCode`() = runTest {
        val ownerId = insertUser("price-owner@test.com")
        val assigneeId = insertUser("price-assignee@test.com")
        val task = buildTask(ownerId, houseId = 1L, assigneeIds = listOf(assigneeId), price = 500.0, currencyCode = "TWD")

        val created = repository.createTask(task)

        assertEquals(500.0, created.price)
        assertEquals("TWD", created.currencyCode)
    }

    @Test
    fun `createTask with null price stores null`() = runTest {
        val ownerId = insertUser("noprice@test.com")
        val assigneeId = insertUser("noprice-assignee@test.com")
        val task = buildTask(ownerId, houseId = 1L, assigneeIds = listOf(assigneeId))

        val created = repository.createTask(task)

        assertNull(created.price)
        assertNull(created.currencyCode)
    }

    @Test
    fun `getTasks returns tasks filtered by houseId`() = runTest {
        val ownerId = insertUser("gettasks@test.com")
        val assigneeId = insertUser("gettasks-a@test.com")
        val task1 = buildTask(ownerId, houseId = 10L, assigneeIds = listOf(assigneeId))
        val task2 = buildTask(ownerId, houseId = 99L, assigneeIds = listOf(assigneeId))

        repository.createTask(task1)
        repository.createTask(task2)

        val result = repository.getTasks(houseId = 10L, size = 20, offset = 0)
        assertEquals(1, result.items.size)
        assertEquals(10L, result.items.first().houseId)
    }

    @Test
    fun `getTasks filters by taskStatus`() = runTest {
        val ownerId = insertUser("status-owner@test.com")
        val assigneeId = insertUser("status-assignee@test.com")
        val todoTask = buildTask(ownerId, houseId = 20L, assigneeIds = listOf(assigneeId))
        val created = repository.createTask(todoTask)
        repository.updateTaskStatus(created.id, TaskStatus.DONE)

        val todos = repository.getTasks(houseId = 20L, taskStatus = TaskStatus.TODO, size = 20, offset = 0)
        val done = repository.getTasks(houseId = 20L, taskStatus = TaskStatus.DONE, size = 20, offset = 0)

        assertEquals(0, todos.items.size)
        assertEquals(1, done.items.size)
    }

    @Test
    fun `getTasksByOwnerId returns only owner tasks`() = runTest {
        val owner1 = insertUser("owner1@test.com")
        val owner2 = insertUser("owner2@test.com")
        val assignee = insertUser("assignee-owner@test.com")

        repository.createTask(buildTask(owner1, houseId = 30L, assigneeIds = listOf(assignee)))
        repository.createTask(buildTask(owner2, houseId = 30L, assigneeIds = listOf(assignee)))

        val result = repository.getTasksByOwnerId(houseId = 30L, ownerId = owner1, size = 20, offset = 0)
        assertEquals(1, result.items.size)
        assertEquals(owner1, result.items.first().ownerId)
    }

    @Test
    fun `getTasksByAssigneeId returns tasks where user is assignee`() = runTest {
        val owner = insertUser("assignee-qowner@test.com")
        val assignee1 = insertUser("a1@test.com")
        val assignee2 = insertUser("a2@test.com")

        repository.createTask(buildTask(owner, houseId = 40L, assigneeIds = listOf(assignee1)))
        repository.createTask(buildTask(owner, houseId = 40L, assigneeIds = listOf(assignee2)))

        val result = repository.getTasksByAssigneeId(houseId = 40L, assigneeId = assignee1, size = 20, offset = 0)
        assertEquals(1, result.items.size)
        assertTrue(result.items.first().assignees.any { it.userId == assignee1 })
    }

    @Test
    fun `isAssignee returns true for assigned user and false otherwise`() = runTest {
        val owner = insertUser("isassignee-owner@test.com")
        val assigned = insertUser("isassignee-a@test.com")
        val notAssigned = insertUser("isassignee-na@test.com")

        val task = repository.createTask(buildTask(owner, houseId = 50L, assigneeIds = listOf(assigned)))

        assertTrue(repository.isAssignee(assigned, task.id))
        assertFalse(repository.isAssignee(notAssigned, task.id))
    }

    @Test
    fun `updateTaskStatus updates status correctly`() = runTest {
        val owner = insertUser("upd-status@test.com")
        val assignee = insertUser("upd-status-a@test.com")
        val task = repository.createTask(buildTask(owner, houseId = 60L, assigneeIds = listOf(assignee)))

        val updated = repository.updateTaskStatus(task.id, TaskStatus.IN_PROGRESS)

        assertEquals(TaskStatus.IN_PROGRESS, updated.taskStatus)
    }

    @Test
    fun `updateTaskAssignStatus updates assignee status`() = runTest {
        val owner = insertUser("assign-status-o@test.com")
        val assignee = insertUser("assign-status-a@test.com")
        val task = repository.createTask(buildTask(owner, houseId = 70L, assigneeIds = listOf(assignee)))

        val updated = repository.updateTaskAssignStatus(task.id, assignee, AssignStatus.ACCEPTED)

        assertEquals(AssignStatus.ACCEPTED, updated.assignees.first { it.userId == assignee }.status)
    }

    @Test
    fun `updateTaskExecutor sets executorId`() = runTest {
        val owner = insertUser("executor-o@test.com")
        val assignee = insertUser("executor-a@test.com")
        val task = repository.createTask(buildTask(owner, houseId = 80L, assigneeIds = listOf(assignee)))

        val updated = repository.updateTaskExecutor(assignee, task.id)

        assertEquals(assignee, updated.executorId)
    }

    @Test
    fun `isTaskOwnerOrExecutor returns true for owner and executor`() = runTest {
        val owner = insertUser("isowner-o@test.com")
        val executor = insertUser("isowner-e@test.com")
        val other = insertUser("isowner-other@test.com")

        val task = repository.createTask(buildTask(owner, houseId = 90L, assigneeIds = listOf(executor)))
        repository.updateTaskExecutor(executor, task.id)

        assertTrue(repository.isTaskOwnerOrExecutor(owner, task.id))
        assertTrue(repository.isTaskOwnerOrExecutor(executor, task.id))
        assertFalse(repository.isTaskOwnerOrExecutor(other, task.id))
    }

    @Test
    fun `updateTaskContent persists all changed fields including price`() = runTest {
        val owner = insertUser("update-content@test.com")
        val assignee = insertUser("update-content-a@test.com")
        val task = repository.createTask(buildTask(owner, houseId = 100L, assigneeIds = listOf(assignee)))

        val modified = task.copy(title = "Updated Title", price = 1200.0, currencyCode = "JPY")
        val result = repository.updateTaskContent(modified)

        assertEquals("Updated Title", result.title)
        assertEquals(1200.0, result.price)
        assertEquals("JPY", result.currencyCode)
    }

    @Test
    fun `getTasks supports pagination`() = runTest {
        val owner = insertUser("paging-owner@test.com")
        val assignee = insertUser("paging-a@test.com")
        repeat(5) {
            repository.createTask(buildTask(owner, houseId = 110L, assigneeIds = listOf(assignee)))
        }

        val page1 = repository.getTasks(houseId = 110L, size = 3, offset = 0)
        val page2 = repository.getTasks(houseId = 110L, size = 3, offset = 3)

        assertEquals(5L, page1.totalCount)
        assertEquals(3, page1.items.size)
        assertEquals(2, page2.items.size)
    }
}
