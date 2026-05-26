package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.dto.InviteCodeResponse
import com.pollyannawu.justwoo.data.HouseInviteRepository

class GenerateInviteCodeUseCase(
    private val repository: HouseInviteRepository,
) {
    suspend operator fun invoke(houseId: Long): InviteCodeResponse =
        repository.generateInviteCode(houseId)
}
