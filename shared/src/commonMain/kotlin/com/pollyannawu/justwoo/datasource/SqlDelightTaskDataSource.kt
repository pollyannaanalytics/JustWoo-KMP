package com.pollyannawu.justwoo.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
import com.pollyannawu.justwoo.db.JustWooDatabase
import com.pollyannawu.justwoo.db.TaskAssigneeEntity
import com.pollyannawu.justwoo.db.TaskEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class SqlDelightTaskDataSource(
    private val db: JustWooDatabase,
    private val dispatcher: CoroutineDispatcher,
) : TaskDataSource {

    private val taskQueries get() = db.taskQueries
    private val assigneeQueries get() = db.taskAssigneeQueries

    override fun getTasks(): Flow<List<Task>> =
        combine(
            taskQueries.selectAll().asFlow().mapToList(dispatcher),
            assigneeQueries.selectAll().asFlow().mapToList(dispatcher),
        ) { tasks, assignees ->
            val byTask = assignees.groupBy { it.taskId }
            tasks.map { entity -> entity.toDomain(byTask[entity.id].orEmpty()) }
        }

    override suspend fun getTaskById(id: Long): Task? = withContext(dispatcher) {
        val entity = taskQueries.selectById(id).executeAsOneOrNull() ?: return@withContext null
        val assignees = assigneeQueries.selectByTaskId(id).executeAsList()
        entity.toDomain(assignees)
    }

    override suspend fun saveTask(task: Task) = upsertOne(task)

    override suspend fun updateTask(task: Task) = upsertOne(task)

    override suspend fun updateTasks(tasks: List<Task>) = withContext(dispatcher) {
        taskQueries.transaction {
            tasks.forEach { writeTaskInTransaction(it) }
        }
    }

    override suspend fun deleteTask(id: Long){
        withContext(dispatcher) {
            taskQueries.deleteById(id)
        }
    }

    private suspend fun upsertOne(task: Task) = withContext(dispatcher) {
        taskQueries.transaction {
            writeTaskInTransaction(task)
        }
    }

    private fun writeTaskInTransaction(task: Task) {
        taskQueries.upsert(task.toEntity())
        assigneeQueries.deleteByTaskId(task.id)
        task.assignees.forEach { a ->
            assigneeQueries.upsert(
                TaskAssigneeEntity(taskId = task.id, userId = a.userId, status = a.status),
            )
        }
    }
}

private fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    accessLevel = accessLevel,
    taskStatus = taskStatus,
    ownerId = ownerId,
    executorId = executorId,
    houseId = houseId,
    dueTime = dueTime,
    createTime = createTime,
    updateTime = updateTime,
    price = price,
    currencyCode = currencyCode,
)

private fun TaskEntity.toDomain(assignees: List<TaskAssigneeEntity>): Task = Task(
    id = id,
    title = title,
    description = description,
    accessLevel = accessLevel,
    taskStatus = taskStatus,
    ownerId = ownerId,
    executorId = executorId,
    houseId = houseId,
    assignees = assignees.map { TaskAssignee(userId = it.userId, status = it.status) },
    dueTime = dueTime,
    createTime = createTime,
    updateTime = updateTime,
    price = price,
    currencyCode = currencyCode,
)
