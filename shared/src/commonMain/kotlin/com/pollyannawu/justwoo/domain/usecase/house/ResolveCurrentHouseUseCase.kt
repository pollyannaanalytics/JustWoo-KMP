package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.data.AuthRepository
import com.pollyannawu.justwoo.data.HouseRepository

/**
 * Fetches the user's first house from the server and saves its id locally.
 * No-op if the user has no house. Called once after authentication to resolve
 * whether to route the user to Home or HouseOnboarding.
 */
class ResolveCurrentHouseUseCase(
    private val houseRepository: HouseRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        val houseId = houseRepository.fetchFirstHouseId() ?: return
        authRepository.setCurrentHouseId(houseId)
    }
}
