package com.pollyannawu.justwoo.domain.usecase.house

import com.pollyannawu.justwoo.core.dto.OtpConfirmRequest
import com.pollyannawu.justwoo.data.HouseInviteRepository

class ConfirmOtpInviteUseCase(private val repository: HouseInviteRepository) {
    suspend operator fun invoke(code: String, displayNumber: Int, houseId: Long) {
        repository.confirmOtpInvite(OtpConfirmRequest(code = code, displayNumber = displayNumber, houseId = houseId))
    }
}
