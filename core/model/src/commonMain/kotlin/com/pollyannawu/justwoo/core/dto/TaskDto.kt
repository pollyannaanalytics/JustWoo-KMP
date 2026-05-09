package com.pollyannawu.justwoo.core.dto

import com.pollyannawu.justwoo.core.AccessLevel
import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.TaskStatus
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val title: String,
    val ownerId: Long,
    val description: String?,
    val houseId: Long,
    val accessLevel: AccessLevel,
    val assigneeIds: List<Long>,
    val dueTime: Instant,
    val price: Double? = null,
    // ISO 4217 currency code, e.g. "TWD", "USD"
    val currencyCode: String? = null
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
    val createTime: String,
    val price: Double? = null,
    val currencyCode: String? = null
)

@Serializable
data class TaskAssigneeResponse(
    val userId: Long,
    val userName: String,
    val avatar: String,
    val status: AssignStatus
)
