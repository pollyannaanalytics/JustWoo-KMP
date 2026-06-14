package com.pollyannawu.justwoo.backend.service

import com.pollyannawu.justwoo.backend.database.utils.PagedResult
import com.pollyannawu.justwoo.backend.repositories.HouseRepository
import com.pollyannawu.justwoo.backend.repositories.ProfileRepository
import com.pollyannawu.justwoo.backend.repositories.TaskRepository
import com.pollyannawu.justwoo.backend.utils.dataresult.TaskDataResult
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TaskServiceTest {

    private val houseRepo: HouseRepository = mockk()
    private val taskRepo: TaskRepository = mockk()
    private val profileRepo: ProfileRepository = mockk()

    private val service: TaskService = DefaultTaskService(houseRepo, taskRepo, profileRepo)

    private val now = Clock.System.now()
    private val houseId = 1L
    private val userId = 10L
    private val taskId = 100L

    private val fakeProfile = Profile(
        id = userId,
        name = "Alice",
        avatar = "avatar.png",
        bankAccount = "123",
        bio = "",
        hashtags = emptyList(),
        createTime = now,
        updateTime = now
    )

    private fun fakeTask(
        id: Long = taskId,
        ownerId: Long = userId,
        executorId: Long = 0L,
        assigneeIds: List<Long> = listOf(userId),
        price: Double? = null,
        currencyCode: String? = null
    ) = Task(
        id = id,
        title = "Test Task",
        description = "Desc",
        accessLevel = AccessLevel.PUBLIC,
        taskStatus = TaskStatus.TODO,
        ownerId = ownerId,
        executorId = executorId,
        houseId = houseId,
        assignees = assigneeIds.map { TaskAssignee(it, AssignStatus.UNASSIGNED) },
        dueTime = now,
        createTime = now,
        updateTime = now,
        price = price,
        currencyCode = currencyCode
    )

    @BeforeEach
    fun setup() {
        coEvery { profileRepo.getProfiles(any()) } returns listOf(fakeProfile)
    }

    // ── createTask ──────────────────────────────────────────────────────────

    @Test
    fun `createTask returns Success when house member`() = runTest {
        val request = CreateTaskRequest(
            title = "Buy groceries",
            ownerId = userId,
            description = "Milk and eggs",
            houseId = houseId,
            accessLevel = AccessLevel.PUBLIC,
            assigneeIds = listOf(userId),
            dueTime = now
        )
        coEvery { houseRepo.isMember(userId, houseId) } returns true
        coEvery { taskRepo.createTask(any()) } returns fakeTask()

        val result = service.createTask(houseId, userId, request)

        assertInstanceOf(TaskDataResult.Success::class.java, result)
    }

    @Test
    fun `createTask returns UserNotAllowed when not a house member`() = runTest {
        val request = CreateTaskRequest(
            title = "Task",
            ownerId = userId,
            description = null,
            houseId = houseId,
            accessLevel = AccessLevel.PUBLIC,
            assigneeIds = listOf(userId),
            dueTime = now
        )
        coEvery { houseRepo.isMember(userId, houseId) } returns false

        val result = service.createTask(houseId, userId, request)

        assertInstanceOf(TaskDataResult.Error.UserNotAllowed::class.java, result)
    }

    @Test
    fun `createTask returns InvalidCurrency for bad currency code`() = runTest {
        val request = CreateTaskRequest(
            title = "Task",
            ownerId = userId,
            description = null,
            houseId = houseId,
            accessLevel = AccessLevel.PUBLIC,
            assigneeIds = listOf(userId),
            dueTime = now,
            price = 100.0,
            currencyCode = "FAKE"
        )
        coEvery { houseRepo.isMember(userId, houseId) } returns true

        val result = service.createTask(houseId, userId, request)

        assertInstanceOf(TaskDataResult.Error.InvalidCurrency::class.java, result)
        assertEquals("FAKE", (result as TaskDataResult.Error.InvalidCurrency).code)
    }

    @Test
    fun `createTask accepts valid ISO 4217 currency code`() = runTest {
        val request = CreateTaskRequest(
            title = "Buy milk",
            ownerId = userId,
            description = null,
            houseId = houseId,
            accessLevel = AccessLevel.PUBLIC,
            assigneeIds = listOf(userId),
            dueTime = now,
            price = 500.0,
            currencyCode = "TWD"
        )
        coEvery { houseRepo.isMember(userId, houseId) } returns true
        coEvery { taskRepo.createTask(any()) } returns fakeTask(price = 500.0, currencyCode = "TWD")

        val result = service.createTask(houseId, userId, request)

        assertInstanceOf(TaskDataResult.Success::class.java, result)
        val response = (result as TaskDataResult.Success).data
        assertEquals(500.0, response.price)
        assertEquals("TWD", response.currencyCode)
    }

    @Test
    fun `createTask with null price succeeds without currency validation`() = runTest {
        val request = CreateTaskRequest(
            title = "Free task",
            ownerId = userId,
            description = null,
            houseId = houseId,
            accessLevel = AccessLevel.PUBLIC,
            assigneeIds = listOf(userId),
            dueTime = now,
            price = null,
            currencyCode = null
        )
        coEvery { houseRepo.isMember(userId, houseId) } returns true
        coEvery { taskRepo.createTask(any()) } returns fakeTask()

        val result = service.createTask(houseId, userId, request)

        assertInstanceOf(TaskDataResult.Success::class.java, result)
        assertNull((result as TaskDataResult.Success).data.price)
    }

    // ── updateTaskContent ────────────────────────────────────────────────────

    @Test
    fun `updateTaskContent returns UserNotAllowed when not a house member`() = runTest {
        coEvery { houseRepo.isMember(userId, houseId) } returns false

        val result = service.updateTaskContent(houseId, userId, fakeTask())

        assertInstanceOf(TaskDataResult.Error.UserNotAllowed::class.java, result)
    }

    @Test
    fun `updateTaskContent returns InvalidCurrency for malformed currency`() = runTest {
        coEvery { houseRepo.isMember(userId, houseId) } returns true

        val result = service.updateTaskContent(houseId, userId, fakeTask(price = 100.0, currencyCode = "xyz"))  // lowercase — invalid format

        assertInstanceOf(TaskDataResult.Error.InvalidCurrency::class.java, result)
        assertEquals("xyz", (result as TaskDataResult.Error.InvalidCurrency).code)
    }

    @Test
    fun `updateTaskContent succeeds with valid currency`() = runTest {
        coEvery { houseRepo.isMember(userId, houseId) } returns true
        coEvery { taskRepo.updateTaskContent(any()) } returns fakeTask(price = 200.0, currencyCode = "USD")

        val result = service.updateTaskContent(houseId, userId, fakeTask(price = 200.0, currencyCode = "USD"))

        assertInstanceOf(TaskDataResult.Success::class.java, result)
    }

    // ── updateTaskStatus ─────────────────────────────────────────────────────

    @Test
    fun `updateTaskStatus returns UserNotAllowed when not owner or executor`() = runTest {
        coEvery { houseRepo.isMember(userId, houseId) } returns true
        coEvery { taskRepo.isTaskOwnerOrExecutor(userId, taskId) } returns false

        val result = service.updateTaskStatus(houseId, userId, taskId, TaskStatus.DONE)

        assertInstanceOf(TaskDataResult.Error.UserNotAllowed::class.java, result)
    }

    @Test
    fun `updateTaskStatus returns Success for owner`() = runTest {
        coEvery { houseRepo.isMember(userId, houseId) } returns true
        coEvery { taskRepo.isTaskOwnerOrExecutor(userId, taskId) } returns true
        coEvery { taskRepo.updateTaskStatus(taskId, TaskStatus.DONE) } returns fakeTask().copy(taskStatus = TaskStatus.DONE)

        val result = service.updateTaskStatus(houseId, userId, taskId, TaskStatus.DONE)

        assertInstanceOf(TaskDataResult.Success::class.java, result)
        assertEquals(TaskStatus.DONE, (result as TaskDataResult.Success).data.status)
    }

    // ── updateTaskAssignStatus ───────────────────────────────────────────────

    @Test
    fun `updateTaskAssignStatus ACCEPTED also sets executor`() = runTest {
        val assignee = TaskAssignee(userId, AssignStatus.ACCEPTED)
        coEvery { houseRepo.isMember(userId, houseId) } returns true
        coEvery { taskRepo.updateTaskAssignStatus(taskId, userId, AssignStatus.ACCEPTED) } returns fakeTask()
        coEvery { taskRepo.updateTaskExecutor(userId, taskId) } returns fakeTask(executorId = userId)

        val result = service.updateTaskAssignStatus(houseId, taskId, assignee)

        assertInstanceOf(TaskDataResult.Success::class.java, result)
    }

    @Test
    fun `updateTaskAssignStatus REJECTED does not set executor`() = runTest {
        val assignee = TaskAssignee(userId, AssignStatus.REJECTED)
        coEvery { houseRepo.isMember(userId, houseId) } returns true
        coEvery { taskRepo.updateTaskAssignStatus(taskId, userId, AssignStatus.REJECTED) } returns fakeTask()

        val result = service.updateTaskAssignStatus(houseId, taskId, assignee)

        assertInstanceOf(TaskDataResult.Success::class.java, result)
    }

    // ── getTaskDetails ───────────────────────────────────────────────────────

    @Test
    fun `getTaskDetails returns UserNotAllowed for non-member`() = runTest {
        coEvery { houseRepo.isMember(userId, houseId) } returns false

        val result = service.getTaskDetails(houseId, userId, page = 1)

        assertInstanceOf(TaskDataResult.Error.UserNotAllowed::class.java, result)
    }

    @Test
    fun `getTaskDetails returns paged response for member`() = runTest {
        coEvery { houseRepo.isMember(userId, houseId) } returns true
        coEvery { taskRepo.getTasks(houseId, null, any(), any()) } returns PagedResult(listOf(fakeTask()), 1L)

        val result = service.getTaskDetails(houseId, userId, page = 1)

        assertInstanceOf(TaskDataResult.Success::class.java, result)
        assertEquals(1, (result as TaskDataResult.Success).data.content.size)
    }
}
