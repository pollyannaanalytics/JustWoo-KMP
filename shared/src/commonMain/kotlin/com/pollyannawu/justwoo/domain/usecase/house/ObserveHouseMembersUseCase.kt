package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.HouseMember
import com.pollyannawu.justwoo.data.HouseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reactive feed of the members in a specific house. Emits an empty list if
 * the house isn't (yet) in cache.
 */
class ObserveHouseMembersUseCase(
    private val houseRepository: HouseRepository,
) {
    operator fun invoke(houseId: Long): Flow<List<HouseMember>> =
        houseRepository.observeHouses().map { houses ->
            houses.firstOrNull { it.id == houseId }?.members.orEmpty()
        }
}
