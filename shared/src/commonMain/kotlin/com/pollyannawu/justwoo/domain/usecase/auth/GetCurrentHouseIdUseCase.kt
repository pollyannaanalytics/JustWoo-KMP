package com.pollyannawu.justwoo.domain.usecase.auth

import com.pollyannawu.justwoo.data.AuthRepository

/** Synchronous read of the persisted house id — `null` if the user hasn't joined a house yet. */
class GetCurrentHouseIdUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Long? = authRepository.getCurrentHouseId()
}
