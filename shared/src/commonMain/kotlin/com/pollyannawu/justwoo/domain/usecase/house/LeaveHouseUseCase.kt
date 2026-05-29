package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.data.HouseRepository
import com.pollyannawu.justwoo.data.datasource.auth.UserStorage

class LeaveHouseUseCase(
    private val houseRepository: HouseRepository,
    private val userStorage: UserStorage,
) {
    suspend operator fun invoke(houseId: Long, userId: Long) {
        houseRepository.removeMember(houseId, userId)
        userStorage.clearHouseId()
    }
}
