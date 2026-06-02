package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.data.AuthRepository

class ChangePasswordUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(oldPassword: String, newPassword: String): Boolean =
        authRepository.changePassword(oldPassword, newPassword)
}
