package com.pollyannawu.justwoo.backend.utils.dataresult

sealed class HouseDataResult<out T> {
    data class Success<T>(val data: T) : HouseDataResult<T>()

    sealed class Error : HouseDataResult<Nothing>() {
        object HouseNotFound : Error()
        data class UserNotAllowed(val id: Long, val type: HouseUserType) : Error()
        data class DatabaseError(val message: String) : Error()
        object NotFound : Error()
    }
}

enum class HouseUserType {
    ADMIN, MEMBER, REQUEST
}