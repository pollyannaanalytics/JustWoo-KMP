package com.pollyannawu.justwoo.core

import kotlinx.serialization.Serializable

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

enum class MemberRole(val value: Int) {
    ADMIN(0), MEMBER(1)
}
