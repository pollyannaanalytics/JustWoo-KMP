package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.core.dto.User
import com.pollyannawu.justwoo.data.AuthRepository
import com.pollyannawu.justwoo.model.AuthDataResult

/**
 * Domain-layer outcome for a sign-in attempt. The use case translates the
 * data-layer [AuthDataResult] into UI-friendly cases so the ViewModel doesn't
 * need to know about HTTP semantics.
 */
sealed class LoginOutcome {
    data class Success(val user: User) : LoginOutcome()

    sealed class Failure : LoginOutcome() {
        /** Email format invalid — caught before hitting the network. */
        data object InvalidEmail : Failure()

        /** Password / email don't match an existing account. */
        data object InvalidCredentials : Failure()

        /** Too many failed attempts; user should wait. */
        data object TooManyAttempts : Failure()

        /** Network / unknown error. */
        data object Network : Failure()
    }
}

/**
 * Validates local input then delegates to [AuthRepository.login].
 * Errors are mapped to [LoginOutcome] so the ViewModel can drop them
 * straight into UI state without any extra branching.
 */
class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): LoginOutcome {
        val trimmedEmail = email.trim()
        if (!trimmedEmail.contains("@")) return LoginOutcome.Failure.InvalidEmail

        return when (val result = authRepository.login(trimmedEmail, password)) {
            is AuthDataResult.Success -> LoginOutcome.Success(result.user)
            AuthDataResult.Failure.InvalidCredentials -> LoginOutcome.Failure.InvalidCredentials
            AuthDataResult.Failure.TooManyAttempts -> LoginOutcome.Failure.TooManyAttempts
            AuthDataResult.Failure.NetworkFailure,
            AuthDataResult.Failure.InvalidRequest,
            AuthDataResult.Failure.EmailAlreadyRegistered -> LoginOutcome.Failure.Network
        }
    }
}
