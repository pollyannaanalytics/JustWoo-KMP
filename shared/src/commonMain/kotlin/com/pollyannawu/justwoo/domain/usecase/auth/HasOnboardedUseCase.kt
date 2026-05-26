package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.data.AuthRepository

/**
 * `true` once any sign-in / sign-up has succeeded on this install. Drives the
 * initial auth screen choice (returning user vs onboarding).
 */
class HasOnboardedUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Boolean = authRepository.hasOnboarded()
}
