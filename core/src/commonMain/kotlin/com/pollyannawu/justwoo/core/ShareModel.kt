package com.pollyannawu.justwoo.core

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
    val passwordHash: String,
    val userRefreshTokenId: Long,
    val createTime: Instant,
    val updateTime: Instant
)

@Serializable
data class UserRefreshToken(
    val id: Long,
    val userId: Long,
    val token: String,
    val deviceId: String,
    val expireAt: Instant,
)

@Serializable
data class Profile(
    val id: Long,
    val name: String,
    val avatar: String,
    val bankAccount: String,
    val updateTime: Instant,
    val createTime: Instant
)

@Serializable
data class House(
    val id: Long,
    val name: String,
    val description: String,
    val avatar: String,
    val members: List<HouseMember>,
    val createTime: kotlinx.datetime.Instant,
    val updateTime: kotlinx.datetime.Instant
)

@Serializable
data class HouseMember(
    val houseId: Long,
    val userId: Long,
    val role: MemberRole,
    val joinedAt: kotlinx.datetime.Instant
)

enum class MemberRole(val value: Int){
    ADMIN(0), MEMBER(1);

}