package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.data.AuthRepository

/**
 * Synchronous read of session state — used to seed the initial value of the
 * reactive `isAuthenticated` flow so first composition picks the right
 * screen without flashing.
 */
class HasActiveSessionUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Boolean = authRepository.hasActiveSession()
}
