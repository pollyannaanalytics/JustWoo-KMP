package com.pollyannawu.justwoo.model

import com.pollyannawu.justwoo.core.dto.User

sealed class AuthDataResult {
    data class Success(val user: User) : AuthDataResult()
    sealed class Failure : AuthDataResult() {
        /** Connectivity error, timeout, parse error, or unknown 5xx. */
        data object NetworkFailure : Failure()

        /** Backend returned 401 PasswordError or 400 HasNoRegisteredUser on login. */
        data object InvalidCredentials : Failure()

        /** Backend returned 409 HasRegisteredUser on register. */
        data object EmailAlreadyRegistered : Failure()

        /** Backend returned 429 OutOfLoginAttemptLimit. */
        data object TooManyAttempts : Failure()

        /** Backend returned 400 BadRequest for malformed input. */
        data object InvalidRequest : Failure()
    }
}

sealed class RefreshTokenResult {
    data class Success(val token: String) : RefreshTokenResult()
    sealed class Failure : RefreshTokenResult() {
        object NetworkFailure : Failure()
        object NoRefreshToken : Failure()
    }
}
