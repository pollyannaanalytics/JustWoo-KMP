package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.data.AuthRepository
import kotlinx.coroutines.flow.Flow

/** Reactive currently-logged-in user id (`null` when signed out). */
class ObserveCurrentUserIdUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<Long?> = authRepository.currentUserId
}
