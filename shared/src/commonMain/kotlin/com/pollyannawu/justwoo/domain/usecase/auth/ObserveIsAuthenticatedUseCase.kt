package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.data.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Reactive sign-in status. Flips to `false` on logout or when the bearer
 * refresh fails and the token pair is cleared — nav can react without
 * polling prefs.
 */
class ObserveIsAuthenticatedUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<Boolean> = authRepository.isAuthenticated
}
