package com.pollyannawu.justwoo.backend.repositories

import com.pollyannawu.justwoo.backend.database.utils.PagedResult
import com.pollyannawu.justwoo.backend.database.utils.dbQuery
import com.pollyannawu.justwoo.backend.database.utils.toPagedRows
import com.pollyannawu.justwoo.backend.schema.Tasks
import com.pollyannawu.justwoo.backend.schema.TasksAssignees
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskStatus
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory

interface TaskRepository {
    suspend fun createTask(task: Task): Task
    suspend fun getTasks(houseId: Long, taskStatus: TaskStatus? = null, size: Int, offset: Long): PagedResult<Task>
    suspend fun getTasksByOwnerId(
        houseId: Long,
        ownerId: Long,
        taskStatus: TaskStatus? = null,
        size: Int,
        offset: Long
    ): PagedResult<Task>

    suspend fun getTasksByAssigneeId(
        houseId: Long,
        assigneeId: Long,
        taskStatus: TaskStatus? = null,
        size: Int,
        offset: Long
    ): PagedResult<Task>

    suspend fun isAssignee(assigneeId: Long, taskId: Long): Boolean

    suspend fun updateTaskStatus(taskId: Long, newStatus: TaskStatus): Task
    suspend fun updateTaskAssignStatus(taskId: Long, userId: Long, assignStatus: AssignStatus): Task

    suspend fun updateTaskExecutor(userId: Long, taskId: Long): Task

    suspend fun isTaskOwnerOrExecutor(ownerId: Long, executerId: Long, taskId: Long): Boolean
    suspend fun updateTaskContent(task: Task): Task

}

internal class DefaultTaskRepository: TaskRepository {

    private val log = LoggerFactory.getLogger("TaskRepository")

    override suspend fun createTask(task: Task): Task = dbQuery {
        log.trace("start createTask")
        val taskId = Tasks.insertAndGetId {
            Tasks.from(it, task)
        }.value

        if (task.assignees.isNotEmpty()) {
            log.trace("task assignees not empty, start to batch and insert")
            TasksAssignees.batchInsert(task.assignees) { assignee ->
                this[TasksAssignees.taskId] = taskId
                this[TasksAssignees.userId] = assignee.userId
                this[TasksAssignees.status] = assignee.status
            }
        } else {
            log.error("task assignees are empty")
        }

        getTaskById(taskId) ?: throw IllegalStateException("task is not created successfully")
    }

    private fun getTaskById(taskId: Long): Task? {
        val rows = (Tasks leftJoin TasksAssignees)
            .selectAll().where { Tasks.id eq taskId }
            .toList()

        if (rows.isEmpty()) return null

        val firstRow = rows.first()

        val assignees = rows.mapNotNull { TasksAssignees.toDomain(it) }

        return Tasks.toDomain(firstRow, assignees)
    }

    override suspend fun getTasks(
        houseId: Long,
        taskStatus: TaskStatus?,
        size: Int,
        offset: Long
    ): PagedResult<Task> = dbQuery {
        log.trace("start find tasks in houseId: {} taskStatus: {}", houseId, taskStatus)
        val query = Tasks.selectAll().where { (Tasks.houseId eq houseId) }
        taskStatus?.let { query.andWhere { Tasks.taskStatus eq it } }

        val pagedRows = query.toPagedRows(size, offset)
        PagedResult(pagedRows.items.toTasks(), pagedRows.totalCount)
    }

    override suspend fun getTasksByOwnerId(
        houseId: Long,
        ownerId: Long,
        taskStatus: TaskStatus?,
        size: Int,
        offset: Long
    ): PagedResult<Task> = dbQuery {
        log.trace(
            "start find tasks in houseId: {} ownerId: {}, taskStatus: {}",
            houseId,
            ownerId,
            taskStatus
        )
        val query = Tasks.selectAll().where { (Tasks.houseId eq houseId) and (Tasks.ownerId eq ownerId) }
        taskStatus?.let { query.andWhere { Tasks.taskStatus eq it } }

        val pagedRows = query.toPagedRows(size, offset)
        PagedResult(pagedRows.items.toTasks(), pagedRows.totalCount)
    }

    override suspend fun getTasksByAssigneeId(
        houseId: Long,
        assigneeId: Long,
        taskStatus: TaskStatus?,
        size: Int,
        offset: Long
    ): PagedResult<Task> = dbQuery {
        log.trace(
            "start find tasks in houseId: {} assigneeId: {}, taskStatus: {}",
            houseId,
            assigneeId,
            taskStatus
        )
        val query = (Tasks leftJoin TasksAssignees)
            .selectAll()
            .where { (Tasks.houseId eq houseId) and (TasksAssignees.userId eq assigneeId) }
        taskStatus?.let { query.andWhere { Tasks.taskStatus eq taskStatus } }

        val pagedRows = query.toPagedRows(size, offset)
        PagedResult(pagedRows.items.toTasks(), pagedRows.totalCount)
    }

    override suspend fun isAssignee(assigneeId: Long, taskId: Long): Boolean = dbQuery{
        val resultRow = TasksAssignees.selectAll()
            .where { (TasksAssignees.userId eq assigneeId) and (TasksAssignees.taskId eq taskId) }
            .toList()

        return@dbQuery resultRow.isNotEmpty()
    }


    override suspend fun updateTaskStatus(taskId: Long, newStatus: TaskStatus): Task = dbQuery {
        Tasks.update({
            Tasks.id eq taskId
        }) {
            it[Tasks.taskStatus] = newStatus
        }
        getTaskById(taskId)
            ?: throw IllegalStateException("Unknown Exception: task has been updated but no task found $taskId")
    }

    override suspend fun updateTaskAssignStatus(
        taskId: Long,
        userId: Long,
        assignStatus: AssignStatus
    ): Task = dbQuery {
        log.trace("start updateTaskAssignStatus")
        TasksAssignees.update({
            (TasksAssignees.taskId eq taskId) and (TasksAssignees.userId eq userId)
        }) {
            it[TasksAssignees.status] = assignStatus
        }
        log.trace("end updateTaskAssignStatus")
        return@dbQuery getTaskById(taskId)
            ?: throw IllegalStateException("Unknown Exception: task assign status has been updated but no task found $taskId")
    }

    override suspend fun updateTaskExecutor(userId: Long, taskId: Long): Task = dbQuery {
        Tasks.update(
            { Tasks.id eq taskId }
        ){
            it[Tasks.executorId] = userId
        }

        return@dbQuery getTaskById(taskId) ?: throw IllegalStateException("Unknown Exception: task has been updated but no task found $taskId")
    }

    override suspend fun isTaskOwnerOrExecutor(ownerId: Long, executerId: Long, taskId: Long): Boolean = dbQuery {
        val resultRow = Tasks.selectAll().where { (Tasks.id eq taskId) and ((Tasks.executorId eq executerId) or (Tasks.ownerId eq ownerId)) }.toList()
        return@dbQuery resultRow.isNotEmpty()
    }

    override suspend fun updateTaskContent(task: Task): Task = dbQuery {
        Tasks.update(
            { Tasks.id eq task.id }
        ) {
            Tasks.from(it, task)
        }

        return@dbQuery getTaskById(task.id)
            ?: throw IllegalStateException("Unknown Exception: task has been updated but no task found ${task.id}")
    }

    private fun List<ResultRow>.toTasks(): List<Task> {
        return this.groupBy { it[Tasks.id] }.map { (_, rows) ->
            Tasks.toDomain(rows.first(), rows.mapNotNull { TasksAssignees.toDomain(it) })
        }
    }
}