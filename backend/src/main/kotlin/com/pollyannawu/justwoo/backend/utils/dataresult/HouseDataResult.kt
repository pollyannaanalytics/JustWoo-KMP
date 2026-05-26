package com.pollyannawu.justwoo.backend.utils.dataresult

sealed class HouseDataResult<out T> {
    data class Success<T>(val data: T) : HouseDataResult<T>()

    sealed class Error : HouseDataResult<Nothing>() {
        object HouseNotFound : Error()
        data class UserNotAllowed(val id: Long, val type: HouseUserType) : Error()
        data class DatabaseError(val message: String) : Error()
        object NotFound : Error()
        object AlreadyMember : Error()
        object InvalidCode : Error()
        object AlreadyProcessed : Error()
        data class BadRequest(val message: String) : Error()
    }
}

enum class HouseUserType {
    ADMIN, MEMBER, REQUEST
}