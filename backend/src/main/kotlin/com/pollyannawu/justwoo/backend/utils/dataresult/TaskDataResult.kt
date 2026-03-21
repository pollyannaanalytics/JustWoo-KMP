package com.pollyannawu.justwoo.backend.utils.dataresult

sealed class TaskDataResult<out T> {
    data class Success<T>(val data: T) : TaskDataResult<T>()

    sealed class Error : TaskDataResult<Nothing>() {
        object HouseNotFound : Error()
        object AssigneeStatusError: Error()
        data class UserNotAllowed(val id: Long, val type: TaskUserType) : Error()
        data class DatabaseError(val message: String) : Error()
        object NotFound : Error()
    }
}

enum class TaskUserType {
    REQUEST, OWNER, ASSIGNEE
}