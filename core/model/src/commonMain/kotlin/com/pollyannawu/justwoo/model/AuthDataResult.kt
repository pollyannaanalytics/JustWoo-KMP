package com.pollyannawu.justwoo.model

import com.pollyannawu.justwoo.core.dto.User

sealed class AuthDataResult {
    data class Success(val user: User) : AuthDataResult()
    sealed class Failure : AuthDataResult() {
        object NetworkFailure : Failure()
    }
}

sealed class RefreshTokenResult {
    data class Success(val token: String) : RefreshTokenResult()
    sealed class Failure : RefreshTokenResult() {
        object NetworkFailure : Failure()
        object NoRefreshToken : Failure()
    }
}
