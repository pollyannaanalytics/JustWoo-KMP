package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.data.HouseInviteRepository

class CreateHouseUseCase(
    private val repository: HouseInviteRepository,
) {
    suspend operator fun invoke(name: String, description: String) {
        repository.createHouse(name, description)
    }
}
