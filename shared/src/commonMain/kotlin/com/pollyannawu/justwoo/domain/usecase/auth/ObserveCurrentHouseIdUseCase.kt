package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.data.AuthRepository
import kotlinx.coroutines.flow.Flow

/** Reactive currently-selected house id (`null` when the user hasn't picked one). */
class ObserveCurrentHouseIdUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<Long?> = authRepository.currentHouseId
}
