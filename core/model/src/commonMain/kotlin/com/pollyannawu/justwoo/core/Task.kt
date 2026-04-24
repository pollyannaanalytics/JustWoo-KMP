package com.pollyannawu.justwoo.core

import com.pollyannawu.justwoo.core.dto.TaskAssigneeResponse
import com.pollyannawu.justwoo.core.dto.TaskResponse
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val accessLevel: AccessLevel,
    var taskStatus: TaskStatus,
    val ownerId: Long,
    val executorId: Long = 0L,
    val houseId: Long,
    val assignees: List<TaskAssignee>,
    val dueTime: Instant,
    val createTime: Instant,
    val updateTime: Instant
)

@Serializable
data class TaskAssignee(
    val userId: Long,
    val status: AssignStatus = AssignStatus.UNASSIGNED
)

fun TaskResponse.toTask() = Task(
    id = this.id,
    title = this.title,
    description = this.description,
    accessLevel = this.accessLevel,
    taskStatus = this.status,
    ownerId = this.ownerId,
    executorId = this.executorId ?: 0L,
    houseId = this.houseId,
    assignees = this.assignees.toTaskAssignees(),
    dueTime = Instant.parse(this.dueTime),
    createTime = Instant.parse(this.createTime),
    updateTime = Instant.parse(this.createTime),
)

fun List<TaskAssigneeResponse>.toTaskAssignees(): List<TaskAssignee> =
    map { TaskAssignee(userId = it.userId, status = it.status) }
