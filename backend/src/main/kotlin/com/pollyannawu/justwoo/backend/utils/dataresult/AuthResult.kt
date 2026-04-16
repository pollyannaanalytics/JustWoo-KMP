package com.pollyannawu.justwoo.backend.utils.dataresult

sealed class AuthDataResult<out T> {
    data class Success<out T>(val data: T? = null) : AuthDataResult<T>()
    sealed class Failure : AuthDataResult<Nothing>() {
        data object HasRegisteredUser : Failure()
        data object InvalidData : Failure()
        data class HasNoRegisteredUser(val email: String) : Failure()
        data object PasswordError : Failure()
        data object OutOfLoginAttemptLimit : Failure()
        data object Unauthorized : Failure()
    }
}