package com.pollyannawu.justwoo.backend.schema

import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.TaskStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

internal object Tasks : LongIdTable("tasks"){

    val title: Column<String> = varchar("title", length = 255)
    val ownerId = long("ownerId")
    val description = text("description")
    val dueTime = datetime("dueTime")

    val accessLevel = customEnumeration(
        name = "accessLevel",
        sql = "INTEGER",
        fromDb = { value ->
            val intValue = value as Int
            AccessLevel.entries.getOrNull(intValue) ?: AccessLevel.PUBLIC
        },
        toDb = { it.ordinal }
    )

    val assignStatus = customEnumeration(
        name = "assignStatus",
        sql = "INTEGER",
        fromDb = { value ->
            val intValue = value as Int
            AssignStatus.entries.getOrNull(intValue) ?: AssignStatus.UNASSIGNED
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
}

internal object TasksAssignees : LongIdTable("tasks_assignees") {
    val taskId = reference("task_id", Tasks, onDelete = ReferenceOption.CASCADE)
    val userId = reference(name = "user_id", foreign = Users, onDelete = ReferenceOption.CASCADE)
}

internal object ChatsTasks: LongIdTable("chats_tasks"){
    val taskId = reference("task_id", Tasks, onDelete = ReferenceOption.CASCADE)
    val chatId = reference("chat_id", Chats, onDelete = ReferenceOption.CASCADE)
}