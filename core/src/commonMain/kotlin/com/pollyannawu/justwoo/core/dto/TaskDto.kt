package com.pollyannawu.justwoo.core

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val title: String,
    val ownerId: Long,
    val description: String?,
    val houseId: Long,
    val accessLevel: AccessLevel,
    val assigneeIds: List<Long>,
    val dueTime: LocalDateTime,
    val createTime: LocalDateTime,
    val updateTime: LocalDateTime
)

@Serializable
data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val accessLevel: AccessLevel,
    val ownerId: Long,
    val executorId: Long?,
    val houseId: Long,
    val assignees: List<TaskAssigneeResponse>,
    val dueTime: String,
    val createTime: String
)

@Serializable
data class TaskAssigneeResponse(
    val userId: Long,
    val userName: String,
    val avatar: String,
    val status: AssignStatus
)