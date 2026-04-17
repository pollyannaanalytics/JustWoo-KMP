package com.pollyannawu.justwoo.backend.schema

import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.TaskStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.statements.UpdateBuilder

internal object Tasks : LongIdTable("tasks") {

    val title: Column<String> = varchar("title", length = 255)
    val ownerId = long("ownerId")

    val houseId = long("house_id")

    var executorId = long("executor_id")
    val description = text("description")
    val dueTime = timestamp("due_time")
    val createTime = timestamp("create_time")
    val updateTime = timestamp("update_time")

    val accessLevel = customEnumeration(
        name = "accessLevel",
        sql = "INTEGER",
        fromDb = { value ->
            val intValue = value as Int
            AccessLevel.entries.getOrNull(intValue) ?: AccessLevel.PUBLIC
        },
        toDb = { it.ordinal }
    )

    val taskStatus = customEnumeration(
        name = "taskStatus",
        sql = "INTEGER",
        fromDb = { value ->
            val intValue = value as Int
            TaskStatus.entries.getOrNull(intValue) ?: TaskStatus.TODO
        },
        toDb = { it.ordinal }
    )


    fun from(it: UpdateBuilder<*>, task: Task) {
        it[title] = task.title
        it[ownerId] = task.ownerId
        it[houseId] = task.houseId
        it[description] = task.description
        it[dueTime] = task.dueTime
        it[executorId] = task.executorId
        it[accessLevel] = task.accessLevel
        it[taskStatus] = task.taskStatus
        it[createTime] = task.createTime
        it[updateTime] = task.updateTime
    }


    fun toDomain(row: ResultRow, assignees: List<TaskAssignee> = emptyList()) = Task(
        id = row[id].value,
        title = row[title],
        ownerId = row[ownerId],
        houseId = row[houseId],
        executorId = row[executorId],
        description = row[description],
        dueTime = row[dueTime],
        accessLevel = row[accessLevel],
        taskStatus = row[taskStatus],
        assignees = assignees,
        createTime = row[createTime],
        updateTime = row[updateTime]
    )
}

internal object TasksAssignees : LongIdTable("tasks_assignees") {
    val taskId = reference("task_id", Tasks, onDelete = ReferenceOption.CASCADE)
    val userId = reference(name = "user_id", foreign = Users, onDelete = ReferenceOption.CASCADE)
    val status = customEnumeration(
        name = "status",
        sql = "INTEGER",
        fromDb = { value ->
            val intValue = value as Int
            AssignStatus.entries.getOrNull(intValue) ?: AssignStatus.UNASSIGNED
        },
        toDb = { it.ordinal }
    )

    fun toDomain(row: ResultRow): TaskAssignee? {
        val uId = row.getOrNull(userId)?.value ?: return null
        return TaskAssignee(
            userId = uId,
            status = row[status]
        )
    }
}

internal object ChatsTasks : LongIdTable("chats_tasks") {
    val taskId = reference("task_id", Tasks, onDelete = ReferenceOption.CASCADE)
    val chatId = reference("chat_id", Chats, onDelete = ReferenceOption.CASCADE)
}