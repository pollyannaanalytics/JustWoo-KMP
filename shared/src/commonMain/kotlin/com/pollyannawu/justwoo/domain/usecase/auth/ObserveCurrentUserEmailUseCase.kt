package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.data.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveCurrentUserEmailUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<String?> = authRepository.currentUserEmail
}
