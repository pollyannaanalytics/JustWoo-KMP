package com.pollyannawu.justwoo.backend.utils.dataresult

sealed class ProfileDataResult<out T> {
    data class Success<T>(val data: T) : ProfileDataResult<T>()

    sealed class Error : ProfileDataResult<Nothing>() {
        object NotFound : Error()
        data class DatabaseError(val message: String) : Error()
    }
}
