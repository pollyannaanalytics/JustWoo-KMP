package com.pollyannawu.justwoo.backend.utils.mapper

import com.pollyannawu.justwoo.core.AssignStatus
import com.pollyannawu.justwoo.core.dto.CreateTaskRequest
import com.pollyannawu.justwoo.core.Profile
import com.pollyannawu.justwoo.core.Task
import com.pollyannawu.justwoo.core.TaskAssignee
import com.pollyannawu.justwoo.core.dto.TaskAssigneeResponse
import com.pollyannawu.justwoo.core.dto.TaskResponse
import com.pollyannawu.justwoo.core.TaskStatus
import com.pollyannawu.justwoo.core.dto.ProfileResponse
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock


fun CreateTaskRequest.toDomain(): Task {
    val now = Clock.System.now()

    return Task(
        id = 0,
        title = this.title,
        ownerId = this.ownerId,
        houseId = this.houseId,
        executorId = 0,
        description = this.description ?: "",
        dueTime = this.dueTime,
        accessLevel = this.accessLevel,
        taskStatus = TaskStatus.TODO,
        assignees = this.assigneeIds.map { userId ->
            TaskAssignee(
                userId = userId,
                status = AssignStatus.UNASSIGNED
            )
        },
        createTime = now,
        updateTime = now
    )
}



fun Task.toResponse(assigneeResponse: List<TaskAssigneeResponse>): TaskResponse {
    return TaskResponse(
        id = this.id,
        title = this.title,
        description = this.description,
        status = this.taskStatus,
        accessLevel = this.accessLevel,
        ownerId = this.ownerId,
        executorId = if (this.executorId == 0L) null else this.executorId,
        houseId = this.houseId,
        assignees = assigneeResponse,
        dueTime = this.dueTime.toString(),
        createTime = this.createTime.toString(),
    )
}

fun TaskAssignee.toResponse(profile: Profile): TaskAssigneeResponse {
    return TaskAssigneeResponse(
        userId = this.userId,
        status = this.status,
        userName = profile.name,
        avatar = profile.avatar,
    )
}


fun Profile.toResponse(): ProfileResponse {
    return ProfileResponse(
        id = this.id,
        name = this.name,
        avatar = this.avatar,
        bankAccount = this.bankAccount,
        createTime = this.createTime,
        updateTime = this.updateTime
    )
}

