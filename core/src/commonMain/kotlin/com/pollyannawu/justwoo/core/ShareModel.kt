package com.pollyannawu.justwoo.core

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: Long,
    val title: String,
    val description: String,
    val accessLevel: AccessLevel,
    val assignStatus: AssignStatus,
    val taskStatus: TaskStatus,
    val ownerId: Long,
    val dueTime: LocalDateTime,
    val assigneeIds: List<Long>
)

@Serializable
enum class AccessLevel { PRIVATE, PUBLIC }

@Serializable
enum class AssignStatus {
    UNASSIGNED,
    PENDING_APPROVAL,
    ACCEPTED,
    REJECTED,
    COMPLETED
}

@Serializable
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}

@Serializable
data class User(
    val id: Long,
    val email: String,
    val password: String
)

@Serializable
data class Profile(
    val id: Long,
    val name: String,
    val avatar: String,
    val bankAccount: String
)

@Serializable
data class House(
    val id: Long,
    val name: String,
    val description: String,
    val avatar: String,
    val memberIds: List<Long>,
    val rules: List<String>
)