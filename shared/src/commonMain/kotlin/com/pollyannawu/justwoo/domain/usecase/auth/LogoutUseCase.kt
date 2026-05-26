package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.data.AuthRepository

/**
 * Clears tokens + cached user from storage. Reactive `isAuthenticated`
 * observers will flip to `false` once the storage flows update.
 */
class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}
