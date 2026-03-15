package com.pollyannawu.justwoo.backend.utils

sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()

    sealed class Error : DataResult<Nothing>() {
        object HouseNotFound : Error()
        object AssigneeStatusError: Error()
        data class UserNotMember(val id: Long, val type: UserType) : Error()
        data class DatabaseError(val message: String) : Error()
        object NotFound : Error()
    }
}

enum class UserType {
    REQUEST, OWNER, ASSIGNEE
}