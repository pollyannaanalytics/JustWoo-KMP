package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.data.HouseRepository
import kotlinx.coroutines.flow.first

/**
 * Snapshot variant of [ObserveHouseMembersUseCase] for iOS / SKIE.
 */
class GetHouseMembersUseCase(
    private val houseRepository: HouseRepository,
) {
    suspend operator fun invoke(houseId: Long): List<HouseMember> {
        houseRepository.refreshHouses()
        val houses = houseRepository.observeHouses().first()
        return houses.firstOrNull { it.id == houseId }?.members.orEmpty()
    }
}
