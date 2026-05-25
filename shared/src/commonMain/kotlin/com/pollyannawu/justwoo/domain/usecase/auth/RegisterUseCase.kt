package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.core.dto.User
import com.pollyannawu.justwoo.data.AuthRepository
import com.pollyannawu.justwoo.model.AuthDataResult

/**
 * Domain-layer outcome for a sign-up attempt. Encompasses both local
 * validation (email/password rules, confirm mismatch) and remote failures
 * so the ViewModel has a single sealed surface to switch over.
 */
sealed class RegisterOutcome {
    data class Success(val user: User) : RegisterOutcome()

    sealed class Failure : RegisterOutcome() {
        data object InvalidEmail : Failure()
        data object WeakPassword : Failure()
        data object PasswordMismatch : Failure()
        data object EmailAlreadyRegistered : Failure()
        data object InvalidRequest : Failure()
        data object Network : Failure()
    }
}

/**
 * Validates local input (email format, password length, confirm match),
 * then delegates to [AuthRepository.register]. The minimum password
 * length lives here so the domain rule has a single owner — the
 * ViewModel surfaces it via [RegisterUseCase.MIN_PASSWORD_LENGTH].
 */
class RegisterUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmPassword: String,
    ): RegisterOutcome {
        val trimmedEmail = email.trim()
        if (!trimmedEmail.contains("@")) return RegisterOutcome.Failure.InvalidEmail
        if (password.length < MIN_PASSWORD_LENGTH) return RegisterOutcome.Failure.WeakPassword
        if (password != confirmPassword) return RegisterOutcome.Failure.PasswordMismatch

        return when (val result = authRepository.register(trimmedEmail, password)) {
            is AuthDataResult.Success -> RegisterOutcome.Success(result.user)
            AuthDataResult.Failure.EmailAlreadyRegistered -> RegisterOutcome.Failure.EmailAlreadyRegistered
            AuthDataResult.Failure.InvalidRequest -> RegisterOutcome.Failure.InvalidRequest
            AuthDataResult.Failure.NetworkFailure,
            AuthDataResult.Failure.InvalidCredentials,
            AuthDataResult.Failure.TooManyAttempts -> RegisterOutcome.Failure.Network
        }
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 10
    }
}
